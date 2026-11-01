package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Position;
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

import static org.junit.jupiter.api.Assertions.*;

class FeatureConnectivityTest {

    private final FeatureConnectivity connectivity =
            new FeatureConnectivity();

    @Test
    void connectedRegionShouldTraverseRoadJunctionL() {
        Board board = junctionBoard();

        FeatureRef centerRoad =
                new FeatureRef(new Position(0, 0), 2);

        FeatureRegion region =
                connectivity.connectedRegion(board, centerRoad);

        assertEquals(FeatureType.ROAD, region.getType());
        assertEquals(4, region.getMembers().size());

        assertTrue(region.contains(
                new FeatureRef(new Position(0, 0), 2)
        ));

        assertTrue(region.contains(
                new FeatureRef(new Position(1, 0), 1)
        ));

        assertTrue(region.contains(
                new FeatureRef(new Position(0, -1), 1)
        ));

        assertTrue(region.contains(
                new FeatureRef(new Position(-1, 0), 1)
        ));
    }

    @Test
    void roadThroughJunctionShouldBeCompleteWhenAllArmsAreClosed() {
        Board board = junctionBoard();

        FeatureRegion region = connectivity.connectedRegion(
                board,
                new FeatureRef(new Position(0, 0), 2)
        );

        assertTrue(connectivity.isComplete(board, region));
    }

    @Test
    void roadThroughJunctionShouldBeIncompleteWhenOneArmIsOpen() {
        Board board = new Board();

        Tile junction = tileById("L");

        board.place(new PlacedTile(
                junction,
                new Position(0, 0),
                Rotation.DEG_0
        ));

        board.place(roadEnd(
                "E",
                new Position(1, 0),
                Direction.WEST
        ));

        board.place(roadEnd(
                "W",
                new Position(-1, 0),
                Direction.EAST
        ));

        FeatureRegion region = connectivity.connectedRegion(
                board,
                new FeatureRef(new Position(0, 0), 2)
        );

        assertFalse(connectivity.isComplete(board, region));
    }

    private Board junctionBoard() {
        Board board = new Board();

        board.place(new PlacedTile(
                tileById("L"),
                new Position(0, 0),
                Rotation.DEG_0
        ));

        board.place(roadEnd(
                "E",
                new Position(1, 0),
                Direction.WEST
        ));

        board.place(roadEnd(
                "S",
                new Position(0, -1),
                Direction.NORTH
        ));

        board.place(roadEnd(
                "W",
                new Position(-1, 0),
                Direction.EAST
        ));

        return board;
    }

    private PlacedTile roadEnd(
            String id,
            Position position,
            Direction contact
    ) {
        Tile tile = new Tile(id, List.of(
                new TileFeature(
                        1,
                        FeatureType.ROAD,
                        0,
                        List.of(
                                new EdgeContact(
                                        contact,
                                        EdgeLane.CENTER
                                )
                        )
                )
        ));

        return new PlacedTile(
                tile,
                position,
                Rotation.DEG_0
        );
    }

    private Tile tileById(String id) {
        return StandardTiles.createStandardDrawTiles()
                .stream()
                .filter(tile -> tile.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}