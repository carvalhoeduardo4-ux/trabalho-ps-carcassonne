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
import com.carcassonne.domain.tile.FieldCityAdjacency;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureConnectivityTest {

    private final FeatureConnectivity connectivity =
            new FeatureConnectivity();

    @Test
    void connectedRegionShouldTraverseRoadJunctionL() {
        Board board =
                junctionBoard();

        FeatureRef centerRoad =
                new FeatureRef(
                        new Position(0, 0),
                        2
                );

        FeatureRegion region =
                connectivity.connectedRegion(
                        board,
                        centerRoad
                );

        assertEquals(
                FeatureType.ROAD,
                region.getType()
        );

        assertEquals(
                4,
                region.getMembers().size()
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                )
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(0, -1),
                                1
                        )
                )
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(-1, 0),
                                1
                        )
                )
        );
    }

    @Test
    void connectedRegionShouldMatchOppositeEdgeLanes() {
        Board board =
                new Board();

        Tile leftTile =
                new Tile(
                        "LEFT",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.FIELD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.EAST,
                                                        EdgeLane.LEFT
                                                )
                                        )
                                ),
                                new TileFeature(
                                        2,
                                        FeatureType.FIELD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.EAST,
                                                        EdgeLane.RIGHT
                                                )
                                        )
                                )
                        )
                );

        Tile rightTile =
                new Tile(
                        "RIGHT",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.FIELD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.WEST,
                                                        EdgeLane.LEFT
                                                )
                                        )
                                ),
                                new TileFeature(
                                        2,
                                        FeatureType.FIELD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.WEST,
                                                        EdgeLane.RIGHT
                                                )
                                        )
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        leftTile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        rightTile,
                        new Position(1, 0),
                        Rotation.DEG_0
                )
        );

        FeatureRegion leftLaneRegion =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                );

        assertEquals(
                2,
                leftLaneRegion
                        .getMembers()
                        .size()
        );

        assertTrue(
                leftLaneRegion.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                )
        );

        assertTrue(
                leftLaneRegion.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                2
                        )
                )
        );

        assertFalse(
                leftLaneRegion.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );

        FeatureRegion rightLaneRegion =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertEquals(
                2,
                rightLaneRegion
                        .getMembers()
                        .size()
        );

        assertTrue(
                rightLaneRegion.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );

        assertFalse(
                rightLaneRegion.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                2
                        )
                )
        );
    }

    @Test
    void straightRoadTilesShouldKeepEastAndWestFieldsSeparated() {
        Board board =
                new Board();

        Tile first =
                tileById("U");

        Tile second =
                tileById("U");

        board.place(
                new PlacedTile(
                        first,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        second,
                        new Position(0, 1),
                        Rotation.DEG_0
                )
        );

        FeatureRegion eastField =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertEquals(
                FeatureType.FIELD,
                eastField.getType()
        );

        assertEquals(
                2,
                eastField.getMembers().size()
        );

        assertTrue(
                eastField.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                )
        );

        assertTrue(
                eastField.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                2
                        )
                )
        );

        assertFalse(
                eastField.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                3
                        )
                )
        );

        assertFalse(
                eastField.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                3
                        )
                )
        );

        FeatureRegion westField =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                3
                        )
                );

        assertEquals(
                2,
                westField.getMembers().size()
        );

        assertTrue(
                westField.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                3
                        )
                )
        );

        assertTrue(
                westField.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                3
                        )
                )
        );

        assertFalse(
                westField.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                2
                        )
                )
        );
    }

    @Test
    void rotatedStraightRoadShouldConnectPhysicalFieldNotFeatureId() {
        Board board =
                new Board();

        Tile bottom =
                tileById("U");

        Tile top =
                tileById("U");

        board.place(
                new PlacedTile(
                        bottom,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        top,
                        new Position(0, 1),
                        Rotation.DEG_180
                )
        );

        FeatureRegion region =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertEquals(
                FeatureType.FIELD,
                region.getType()
        );

        assertEquals(
                2,
                region.getMembers().size()
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                )
        );

        assertTrue(
                region.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                3
                        )
                )
        );

        assertFalse(
                region.contains(
                        new FeatureRef(
                                new Position(0, 1),
                                2
                        )
                )
        );
    }

    @Test
    void adjacentCityRegionsShouldFindCitiesAdjacentToConnectedStandardField() {
        Board board =
                new Board();

        Tile first =
                tileById("D");

        Tile second =
                tileById("D");

        /*
         * Duas peças D lado a lado:
         *
         * CITY             CITY
         *  #1               #1
         *
         * FIELD#4 <------> FIELD#4
         *     ROAD ------- ROAD
         *
         * O campo norte forma uma região única,
         * mas toca duas cidades diferentes.
         */
        board.place(
                new PlacedTile(
                        first,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        second,
                        new Position(1, 0),
                        Rotation.DEG_0
                )
        );

        FeatureRegion field =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                4
                        )
                );

        assertEquals(
                FeatureType.FIELD,
                field.getType()
        );

        assertEquals(
                2,
                field.getMembers().size()
        );

        Set<FeatureRegion> cities =
                connectivity.adjacentCityRegions(
                        board,
                        field
                );

        assertEquals(
                2,
                cities.size()
        );

        assertTrue(
                containsRegionWith(
                        cities,
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                )
        );

        assertTrue(
                containsRegionWith(
                        cities,
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );
    }

    @Test
    void adjacentCityRegionsShouldDeduplicateSameConnectedCity() {
        Board board =
                new Board();

        /*
         * Montamos duas peças sintéticas.
         *
         * Tanto os campos quanto as cidades atravessam
         * a borda compartilhada.
         *
         * Cada campo local declara adjacência à sua
         * cidade local.
         *
         * FIELD A <------> FIELD B
         * CITY  A <------> CITY  B
         *
         * Apesar de a cidade ser encontrada por dois
         * membros diferentes do campo, ela representa
         * uma única FeatureRegion.
         */
        TileFeature leftCity =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.CENTER
                                )
                        )
                );

        TileFeature leftField =
                new TileFeature(
                        2,
                        FeatureType.FIELD,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.LEFT
                                ),
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.RIGHT
                                )
                        )
                );

        Tile left =
                new Tile(
                        "LEFT",
                        List.of(
                                leftCity,
                                leftField
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        leftField,
                                        leftCity
                                )
                        )
                );

        TileFeature rightCity =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.CENTER
                                )
                        )
                );

        TileFeature rightField =
                new TileFeature(
                        2,
                        FeatureType.FIELD,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.LEFT
                                ),
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.RIGHT
                                )
                        )
                );

        Tile right =
                new Tile(
                        "RIGHT",
                        List.of(
                                rightCity,
                                rightField
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        rightField,
                                        rightCity
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        left,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        right,
                        new Position(1, 0),
                        Rotation.DEG_0
                )
        );

        FeatureRegion field =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertEquals(
                2,
                field.getMembers().size()
        );

        Set<FeatureRegion> cities =
                connectivity.adjacentCityRegions(
                        board,
                        field
                );

        /*
         * Os dois FieldCityAdjacency apontam para
         * partes da MESMA cidade conectada.
         */
        assertEquals(
                1,
                cities.size()
        );

        FeatureRegion city =
                cities.iterator().next();

        assertEquals(
                FeatureType.CITY,
                city.getType()
        );

        assertEquals(
                2,
                city.getMembers().size()
        );

        assertTrue(
                city.contains(
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                )
        );

        assertTrue(
                city.contains(
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );
    }

    @Test
    void adjacentCityRegionsShouldRejectNonFieldRegion() {
        Board board =
                new Board();

        Tile tile =
                tileById("E");

        board.place(
                new PlacedTile(
                        tile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        FeatureRegion city =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                );

        assertEquals(
                FeatureType.CITY,
                city.getType()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        connectivity
                                .adjacentCityRegions(
                                        board,
                                        city
                                )
        );
    }

    @Test
    void roadThroughJunctionShouldBeCompleteWhenAllArmsAreClosed() {
        Board board =
                junctionBoard();

        FeatureRegion region =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertTrue(
                connectivity.isComplete(
                        board,
                        region
                )
        );
    }

    @Test
    void roadThroughJunctionShouldBeIncompleteWhenOneArmIsOpen() {
        Board board =
                new Board();

        Tile junction =
                tileById("L");

        board.place(
                new PlacedTile(
                        junction,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                roadEnd(
                        "E",
                        new Position(1, 0),
                        Direction.WEST
                )
        );

        board.place(
                roadEnd(
                        "W",
                        new Position(-1, 0),
                        Direction.EAST
                )
        );

        FeatureRegion region =
                connectivity.connectedRegion(
                        board,
                        new FeatureRef(
                                new Position(0, 0),
                                2
                        )
                );

        assertFalse(
                connectivity.isComplete(
                        board,
                        region
                )
        );
    }

    private boolean containsRegionWith(
            Set<FeatureRegion> regions,
            FeatureRef feature
    ) {
        return regions
                .stream()
                .anyMatch(
                        region ->
                                region.contains(feature)
                );
    }

    private Board junctionBoard() {
        Board board =
                new Board();

        board.place(
                new PlacedTile(
                        tileById("L"),
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                roadEnd(
                        "E",
                        new Position(1, 0),
                        Direction.WEST
                )
        );

        board.place(
                roadEnd(
                        "S",
                        new Position(0, -1),
                        Direction.NORTH
                )
        );

        board.place(
                roadEnd(
                        "W",
                        new Position(-1, 0),
                        Direction.EAST
                )
        );

        return board;
    }

    private PlacedTile roadEnd(
            String id,
            Position position,
            Direction contact
    ) {
        Tile tile =
                new Tile(
                        id,
                        List.of(
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
                        )
                );

        return new PlacedTile(
                tile,
                position,
                Rotation.DEG_0
        );
    }

    private Tile tileById(String id) {
        return StandardTiles
                .createStandardDrawTiles()
                .stream()
                .filter(
                        tile ->
                                tile.getId()
                                        .equals(id)
                )
                .findFirst()
                .orElseThrow();
    }
}