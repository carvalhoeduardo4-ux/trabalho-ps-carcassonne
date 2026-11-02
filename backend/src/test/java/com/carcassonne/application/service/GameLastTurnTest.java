package com.carcassonne.application.service;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.game.GameStatus;
import com.carcassonne.domain.game.Turn;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameLastTurnTest {

    @ParameterizedTest
    @EnumSource(FeatureType.class)
    void lastPlacementWaitsForMeepleThenScoresRemainingRegionAndFinishes(FeatureType type) {
        Tile last = tileWith(type);
        GameEngine engine = engineWith(List.of(last));
        Game game = engine.getGame();
        assertEquals(last, engine.drawTile().orElseThrow());
        assertFalse(game.hasTilesRemaining());
        assertThrows(InvalidTurnPhaseException.class, engine::drawTile);
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        engine.placeTile(new Placement(new Position(0, 1), Rotation.DEG_0));
        assertThrows(InvalidTurnPhaseException.class, engine::drawTile);
        assertThrows(InvalidTurnPhaseException.class, game::scoreEndGame);
        assertThrows(InvalidTurnPhaseException.class, game::finish);
        assertEquals(0, game.currentPlayer().getScore());
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        engine.placeMeeple(new FeatureRef(new Position(0, 1), 1));
        int expected = switch (type) {
            case ROAD -> 1;
            case CITY, MONASTERY -> 2;
            case FIELD -> 0;
        };
        assertFinished(engine, expected, 0);
        assertThrows(InvalidTurnPhaseException.class,
                () -> engine.placeMeeple(new FeatureRef(new Position(0, 1), 1)));
        assertEquals(expected, game.currentPlayer().getScore());
    }

    @Test
    void lastPlacementCanFinishBySkippingMeeple() {
        GameEngine engine = engineWith(List.of(tileWith(FeatureType.FIELD)));
        engine.drawTile();
        engine.placeTile(new Placement(new Position(0, 1), Rotation.DEG_0));
        engine.skipMeeple();
        assertFinished(engine, 0, 0);
    }

    @Test
    void impossibleTilesAreReplacedInSameTurnAndFinalDiscardScoresExistingMeeples() {
        Tile impossible = new Tile("ALL-CITY", List.of(new TileFeature(1, FeatureType.CITY, 0,
                Arrays.stream(Direction.values()).map(d -> new EdgeContact(d, EdgeLane.CENTER)).toList())));
        Tile monastery = tileWith(FeatureType.MONASTERY);
        GameEngine engine = engineWith(List.of(impossible, monastery, impossible, impossible));
        Game game = engine.getGame();
        assertEquals(monastery, engine.drawTile().orElseThrow());
        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(2, game.getDeck().remaining());
        engine.placeTile(new Placement(new Position(0, 1), Rotation.DEG_0));
        engine.placeMeeple(new FeatureRef(new Position(0, 1), 1));
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(6, game.availableMeeples(game.getPlayers().get(0)));
        assertEquals(1, game.getCurrentPlayerIndex());
        assertTrue(engine.drawTile().isEmpty());
        assertTrue(game.currentDrawnTile().isEmpty());
        assertTrue(game.currentPlacedTile().isEmpty());
        assertEquals(2, game.getBoard().getTiles().size());
        assertFinished(engine, 2, 1);
    }

    private void assertFinished(GameEngine engine, int aliceScore, int currentPlayerIndex) {
        Game game = engine.getGame();
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(currentPlayerIndex, game.getCurrentPlayerIndex());
        assertEquals(List.of(aliceScore, 0), game.getPlayers().stream().map(Player::getScore).toList());
        assertTrue(game.getMeeples().isEmpty());
        game.getPlayers().forEach(player -> assertEquals(7, game.availableMeeples(player)));
        assertThrows(InvalidTurnPhaseException.class, engine::drawTile);
        assertThrows(InvalidTurnPhaseException.class, engine::skipMeeple);
        assertThrows(InvalidTurnPhaseException.class, game::scoreCompletedFeatures);
        assertThrows(InvalidTurnPhaseException.class, game::scoreEndGame);
        assertThrows(InvalidTurnPhaseException.class, game::nextPlayer);
        assertThrows(InvalidTurnPhaseException.class, game::finish);
        assertEquals(List.of(aliceScore, 0), game.getPlayers().stream().map(Player::getScore).toList());
    }

    private GameEngine engineWith(List<Tile> tiles) {
        Board board = new Board();
        board.place(new PlacedTile(tileWith(FeatureType.FIELD),
                new Placement(new Position(0, 0), Rotation.DEG_0)));
        Game game = new Game(board, new TileDeck(tiles), List.of(
                new Player("Alice", PlayerColor.RED), new Player("Bob", PlayerColor.BLUE)), new Turn());
        GameEngine engine = new GameEngine(new TilePlacementRules(), new MeeplePlacementRules(), new ScoringRules());
        ReflectionTestUtils.setField(engine, "game", game);
        return engine;
    }

    private Tile tileWith(FeatureType type) {
        List<EdgeContact> fieldContacts = Arrays.stream(Direction.values())
                .filter(d -> type != FeatureType.ROAD || (d != Direction.EAST && d != Direction.WEST))
                .filter(d -> type != FeatureType.CITY || d != Direction.NORTH)
                .map(d -> new EdgeContact(d, EdgeLane.CENTER)).toList();
        TileFeature field = new TileFeature(type == FeatureType.FIELD ? 1 : 2, FeatureType.FIELD, 0, fieldContacts);
        List<EdgeContact> featureContacts = switch (type) {
            case ROAD -> List.of(new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER));
            case CITY -> List.of(new EdgeContact(Direction.NORTH, EdgeLane.CENTER));
            default -> List.of();
        };
        return new Tile(type.name(), type == FeatureType.FIELD ? List.of(field) : List.of(field,
                new TileFeature(1, type, type == FeatureType.CITY ? 1 : 0, featureContacts)));
    }
}
