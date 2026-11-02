package com.carcassonne.domain.game;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.exception.InvalidPlacementException;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.FieldCityAdjacency;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameProtectionTest {

    @Test
    void constructorAndAccessorsKeepIndependentSnapshots() {
        Tile field = fieldTile();
        Board board = boardWith(field);
        TileDeck deck = new TileDeck(List.of(field, field));
        Turn turn = new Turn();
        Player alice = new Player("Alice", PlayerColor.RED);
        List<Player> players = new ArrayList<>(List.of(alice, new Player("Bob", PlayerColor.BLUE)));
        Game game = new Game(board, deck, players, turn);

        board.place(placed(field, 9, 9));
        deck.draw();
        turn.recordDrawnTile(field);
        alice.addScore(100);
        players.clear();

        Board boardSnapshot = game.getBoard();
        TileDeck deckSnapshot = game.getDeck();
        Turn turnSnapshot = game.getTurn();
        List<Player> playerSnapshots = game.getPlayers();
        Player currentSnapshot = game.currentPlayer();
        boardSnapshot.place(placed(field, 8, 8));
        deckSnapshot.shuffle();
        deckSnapshot.draw();
        turnSnapshot.recordDrawnTile(field);
        turnSnapshot.recordPlacedTile(placed(field, 8, 8));
        turnSnapshot.resolveMeeple();
        playerSnapshots.get(0).addScore(100);
        currentSnapshot.addScore(100);
        game.winners().get(0).addScore(100);
        assertThrows(UnsupportedOperationException.class, () -> playerSnapshots.clear());

        assertEquals(1, game.getBoard().getTiles().size());
        assertEquals(2, game.getDeck().remaining());
        assertEquals(2, game.getPlayers().size());
        assertEquals(List.of(0, 0), scores(game));
        assertInstanceOf(DrawTileState.class, game.getTurn().getState());
        assertTrue(game.currentDrawnTile().isEmpty());
        assertRejected(game, game::nextPlayer);

        game.drawTile();
        Turn drawnSnapshot = game.getTurn();
        drawnSnapshot.discardDrawnTile();
        assertInstanceOf(PlaceTileState.class, game.getTurn().getState());
        game.placeTile(placement(1, 0));
        Turn placedSnapshot = game.getTurn();
        placedSnapshot.resolveMeeple();
        placedSnapshot.reset();
        assertRejected(game, game::scoreCompletedFeatures);
        game.placeMeeple(new FeatureRef(new Position(1, 0), 1));
        List<MeeplePlacement> meepleSnapshots = game.getMeeples();
        meepleSnapshots.get(0).getPlayer().addScore(100);
        assertThrows(UnsupportedOperationException.class, meepleSnapshots::clear);
        assertEquals(List.of(0, 0), scores(game));
        assertEquals(6, game.availableMeeples(currentSnapshot));
        game.scoreCompletedFeatures();
        game.nextPlayer();
        assertEquals(1, game.getCurrentPlayerIndex());
        assertFalse(boardSnapshot.isOccupied(new Position(1, 0)));
        assertInstanceOf(PlaceMeepleState.class, turnSnapshot.getState());
    }

    @Test
    void tileGeometryCannotBeChangedThroughSnapshotsOrConstructorLists() {
        List<EdgeContact> contacts = new ArrayList<>(List.of(new EdgeContact(Direction.EAST, EdgeLane.CENTER)));
        TileFeature field = new TileFeature(1, FeatureType.FIELD, 0, contacts);
        TileFeature city = new TileFeature(2, FeatureType.CITY, 0, List.of());
        List<TileFeature> features = new ArrayList<>(List.of(field, city));
        List<FieldCityAdjacency> adjacencies = new ArrayList<>(List.of(new FieldCityAdjacency(field, city)));
        Tile tile = new Tile("SNAPSHOT", features, adjacencies);
        Game game = new Game(boardWith(tile), new TileDeck(List.of(tile)), players(), new Turn());
        contacts.clear();
        features.clear();
        adjacencies.clear();

        List<Tile> exposedTiles = List.of(
                game.getBoard().tileAt(new Position(0, 0)).orElseThrow().getTile(),
                game.getDeck().getRemainingTiles().get(0), game.drawTile(),
                game.getTurn().drawnTile().orElseThrow(), game.currentDrawnTile().orElseThrow());
        for (Tile exposed : exposedTiles) {
            assertEquals(2, exposed.getFeatures().size());
            assertEquals(1, exposed.getFieldCityAdjacencies().size());
            assertEquals(1, exposed.feature(1).getEdgeContacts().size());
            assertThrows(UnsupportedOperationException.class, () -> exposed.getFeatures().clear());
            assertThrows(UnsupportedOperationException.class, () -> exposed.getFieldCityAdjacencies().clear());
            assertThrows(UnsupportedOperationException.class, () -> exposed.feature(1).getEdgeContacts().clear());
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void lifecycleRequiresMeepleDecisionAndExactlyOneScoringPerTurn(boolean lastTile, boolean placeMeeple) {
        Tile field = fieldTile();
        Game game = new Game(boardWith(field),
                new TileDeck(lastTile ? List.of(field) : List.of(field, field)), players(), new Turn());
        assertLifecycleBlocked(game);
        game.drawTile();
        assertLifecycleBlocked(game);
        assertThrows(InvalidPlacementException.class, game::discardDrawnTile);
        game.placeTile(placement(1, 0));
        assertLifecycleBlocked(game);
        if (placeMeeple) {
            game.placeMeeple(new FeatureRef(new Position(1, 0), 1));
        } else {
            game.skipMeeple();
        }
        assertRejected(game, game::nextPlayer);
        assertRejected(game, game::scoreEndGame);
        assertRejected(game, game::finish);
        assertRejected(game, game::skipMeeple);
        game.scoreCompletedFeatures();
        assertRejected(game, game::scoreCompletedFeatures);
        assertRejected(game, game::finish);
        if (lastTile) {
            assertRejected(game, game::nextPlayer);
            game.scoreEndGame();
            assertEquals(7, game.availableMeeples(game.currentPlayer()));
            assertRejected(game, game::scoreEndGame);
            assertRejected(game, game::scoreCompletedFeatures);
            assertRejected(game, game::nextPlayer);
            game.finish();
            assertLifecycleBlocked(game);
        } else {
            assertRejected(game, game::scoreEndGame);
            game.nextPlayer();
            assertEquals(1, game.getCurrentPlayerIndex());
            assertLifecycleBlocked(game);
            game.drawTile();
            game.placeTile(placement(2, 0));
            game.skipMeeple();
            game.scoreCompletedFeatures();
            game.scoreEndGame();
            game.finish();
        }
        assertEquals(GameStatus.FINISHED, game.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = FeatureType.class, names = {"ROAD", "CITY"})
    void completedRegionScoresOnceAndSnapshotsKeepTheirOriginalScores(FeatureType type) {
        Tile first = endTile(type, Direction.EAST);
        Tile last = endTile(type, Direction.WEST);
        Game game = new Game(boardWith(first), new TileDeck(List.of(last)), players(), new Turn());
        game.drawTile();
        game.placeTile(placement(1, 0));
        game.placeMeeple(new FeatureRef(new Position(1, 0), 1));
        Player currentSnapshot = game.currentPlayer();
        List<Player> playerSnapshots = game.getPlayers();
        List<MeeplePlacement> meepleSnapshots = game.getMeeples();
        assertRejected(game, game::scoreEndGame);
        game.scoreCompletedFeatures();
        int expected = type == FeatureType.CITY ? 8 : 2;
        assertEquals(expected, game.currentPlayer().getScore());
        assertEquals(0, currentSnapshot.getScore());
        assertEquals(0, playerSnapshots.get(0).getScore());
        assertEquals(0, meepleSnapshots.get(0).getPlayer().getScore());
        assertEquals(1, meepleSnapshots.size());
        assertTrue(game.getMeeples().isEmpty());
        assertEquals(7, game.availableMeeples(currentSnapshot));
        assertRejected(game, game::scoreCompletedFeatures);
        game.scoreEndGame();
        assertEquals(expected, game.currentPlayer().getScore());
        assertRejected(game, game::scoreEndGame);
        assertRejected(game, game::scoreCompletedFeatures);
        game.finish();
        assertLifecycleBlocked(game);
    }

    @Test
    void neighboringMonasteryScoresOnceWhenLastTileCompletesIt() {
        Board board = new Board();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x != 0 || y != 0) && (x != 1 || y != 1)) {
                    board.place(placed(fieldTile(), x, y));
                }
            }
        }
        Tile monastery = new Tile("MONASTERY", List.of(fieldTile().feature(1),
                new TileFeature(2, FeatureType.MONASTERY, 0, List.of())));
        Game game = new Game(board, new TileDeck(List.of(monastery, fieldTile())), players(), new Turn());
        game.drawTile();
        game.placeTile(placement(0, 0));
        game.placeMeeple(new FeatureRef(new Position(0, 0), 2));
        game.scoreCompletedFeatures();
        assertEquals(0, game.currentPlayer().getScore());
        assertEquals(6, game.availableMeeples(game.currentPlayer()));
        game.nextPlayer();
        game.drawTile();
        game.placeTile(placement(1, 1));
        assertLifecycleBlocked(game);
        game.skipMeeple();
        game.scoreCompletedFeatures();
        assertEquals(List.of(9, 0), scores(game));
        assertTrue(game.getMeeples().isEmpty());
        assertRejected(game, game::scoreCompletedFeatures);
        game.scoreEndGame();
        assertEquals(List.of(9, 0), scores(game));
        assertRejected(game, game::scoreEndGame);
        game.finish();
    }

    @Test
    void finalDiscardAllowsEndScoringOnlyAfterDiscardingAndNeverAdvancesPlayer() {
        Tile field = fieldTile();
        Tile impossible = new Tile("CITY", List.of(new TileFeature(1, FeatureType.CITY, 0,
                Arrays.stream(Direction.values()).map(d -> new EdgeContact(d, EdgeLane.CENTER)).toList())));
        TileFeature city = new TileFeature(2, FeatureType.CITY, 0, List.of());
        Tile fieldWithCity = new Tile("FIELD-CITY", List.of(field.feature(1), city),
                List.of(new FieldCityAdjacency(field.feature(1), city)));
        Game game = new Game(boardWith(field), new TileDeck(List.of(fieldWithCity, impossible)), players(), new Turn());
        game.drawTile();
        game.placeTile(placement(1, 0));
        game.placeMeeple(new FeatureRef(new Position(1, 0), 1));
        game.scoreCompletedFeatures();
        assertEquals(6, game.availableMeeples(game.currentPlayer()));
        assertEquals(List.of(0, 0), scores(game));
        game.nextPlayer();
        game.drawTile();
        assertTrue(game.legalPlacements().isEmpty());
        assertLifecycleBlocked(game);
        game.discardDrawnTile();
        assertRejected(game, game::nextPlayer);
        assertRejected(game, game::scoreCompletedFeatures);
        assertRejected(game, game::finish);
        assertEquals(1, game.getCurrentPlayerIndex());
        game.scoreEndGame();
        assertEquals(List.of(3, 0), scores(game));
        assertTrue(game.getMeeples().isEmpty());
        assertRejected(game, game::scoreEndGame);
        game.finish();
        assertLifecycleBlocked(game);
    }

    private void assertLifecycleBlocked(Game game) {
        assertRejected(game, game::nextPlayer);
        assertRejected(game, game::scoreCompletedFeatures);
        assertRejected(game, game::scoreEndGame);
        assertRejected(game, game::finish);
    }

    private void assertRejected(Game game, Executable action) {
        List<Integer> scores = scores(game);
        List<PlacedTile> tiles = List.copyOf(game.getBoard().getTiles());
        List<Tile> deck = game.getDeck().getRemainingTiles();
        List<MeeplePlacement> meeples = game.getMeeples();
        var drawn = game.currentDrawnTile();
        var placed = game.currentPlacedTile();
        int index = game.getCurrentPlayerIndex();
        GameStatus status = game.getStatus();
        assertThrows(InvalidTurnPhaseException.class, action);
        assertEquals(scores, scores(game));
        assertEquals(tiles, List.copyOf(game.getBoard().getTiles()));
        assertEquals(deck, game.getDeck().getRemainingTiles());
        assertEquals(meeples, game.getMeeples());
        assertEquals(drawn, game.currentDrawnTile());
        assertEquals(placed, game.currentPlacedTile());
        assertEquals(index, game.getCurrentPlayerIndex());
        assertEquals(status, game.getStatus());
    }

    private List<Integer> scores(Game game) {
        return game.getPlayers().stream().map(Player::getScore).toList();
    }

    private List<Player> players() {
        return List.of(new Player("Alice", PlayerColor.RED), new Player("Bob", PlayerColor.BLUE));
    }

    private Tile fieldTile() {
        return new Tile("FIELD", List.of(new TileFeature(1, FeatureType.FIELD, 0,
                Arrays.stream(Direction.values()).map(d -> new EdgeContact(d, EdgeLane.CENTER)).toList())));
    }

    private Tile endTile(FeatureType type, Direction direction) {
        return new Tile(type.name(), List.of(new TileFeature(1, type, type == FeatureType.CITY ? 1 : 0,
                List.of(new EdgeContact(direction, EdgeLane.CENTER)))));
    }

    private Board boardWith(Tile tile) {
        Board board = new Board();
        board.place(placed(tile, 0, 0));
        return board;
    }

    private Placement placement(int x, int y) {
        return new Placement(new Position(x, y), Rotation.DEG_0);
    }

    private PlacedTile placed(Tile tile, int x, int y) {
        return new PlacedTile(tile, placement(x, y));
    }
}
