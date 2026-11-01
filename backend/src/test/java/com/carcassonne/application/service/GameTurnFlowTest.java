package com.carcassonne.application.service;

import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.exception.InvalidPlacementException;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.game.GameStatus;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.rules.CityScoringStrategy;
import com.carcassonne.domain.rules.FeatureConnectivity;
import com.carcassonne.domain.rules.FieldScoringStrategy;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.MonasteryScoringStrategy;
import com.carcassonne.domain.rules.RoadScoringStrategy;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameTurnFlowTest {

    private GameEngine newEngine() {
        FeatureConnectivity connectivity = new FeatureConnectivity();
        ScoringRules scoring = new ScoringRules(connectivity, new RoadScoringStrategy(),
                new CityScoringStrategy(), new MonasteryScoringStrategy(), new FieldScoringStrategy(connectivity));
        return new GameEngine(new TilePlacementRules(), new MeeplePlacementRules(connectivity), scoring);
    }

    private List<PlayerSetup> twoPlayers() {
        return List.of(new PlayerSetup("A", PlayerColor.values()[0]), new PlayerSetup("B", PlayerColor.values()[1]));
    }

    @Test
    void straightRoadMustBeRotatedToMatchStarterRoad() {
        Board board = new Board();
        board.place(new PlacedTile(StandardTiles.createStarterTile(), new Position(0, 0), Rotation.DEG_0));
        Tile straight = new Tile(List.of(
                new TileFeature(1, FeatureType.ROAD, 0, List.of(
                        new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                        new EdgeContact(Direction.SOUTH, EdgeLane.CENTER))),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                        new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                        new EdgeContact(Direction.WEST, EdgeLane.CENTER)))));
        TilePlacementRules rules = new TilePlacementRules();
        Position east = new Position(1, 0);
        assertFalse(rules.canPlace(board, straight, new Placement(east, Rotation.DEG_0)));
        assertTrue(rules.canPlace(board, straight, new Placement(east, Rotation.DEG_90)));
        assertFalse(rules.canPlace(board, straight, new Placement(new Position(5, 5), Rotation.DEG_0)));
        assertFalse(rules.canPlace(board, straight, new Placement(new Position(0, 0), Rotation.DEG_0)));
    }

    @Test
    void actionsOutOfOrderAreRejected() {
        GameEngine engine = newEngine();
        engine.startGame(twoPlayers());
        assertThrows(InvalidTurnPhaseException.class, engine::skipMeeple);
        assertThrows(InvalidTurnPhaseException.class,
                () -> engine.placeTile(new Placement(new Position(0, 1), Rotation.DEG_0)));
        engine.drawTile();
        assertThrows(InvalidTurnPhaseException.class, engine::drawTile);
    }

    @Test
    void invalidPlacementIsRejected() {
        GameEngine engine = newEngine();
        engine.startGame(twoPlayers());
        engine.drawTile();
        assertThrows(InvalidPlacementException.class,
                () -> engine.placeTile(new Placement(new Position(9, 9), Rotation.DEG_0)));
    }

    @Test
    void playsAFullGameUntilDeckIsExhausted() {
        GameEngine engine = newEngine();
        engine.startGame(twoPlayers());
        int turns = 0;
        while (engine.getGame().getStatus() == GameStatus.ACTIVE) {
            Optional<Tile> tile = engine.drawTile();
            if (tile.isEmpty()) {
                break;
            }
            List<Placement> placements = engine.getLegalPlacements();
            assertFalse(placements.isEmpty());
            engine.placeTile(placements.get(0));
            List<FeatureRef> features = engine.getLegalMeepleFeatures();
            if (turns % 2 == 0 && !features.isEmpty()) {
                engine.placeMeeple(features.get(0));
            } else {
                engine.skipMeeple();
            }
            turns++;
        }
        assertEquals(GameStatus.FINISHED, engine.getGame().getStatus());
        assertTrue(turns > 0);
        assertTrue(engine.getGame().getBoard().getTiles().size() > 1);
        engine.getGame().getPlayers().forEach(p -> assertTrue(engine.getGame().availableMeeples(p) >= 0));
    }
}
