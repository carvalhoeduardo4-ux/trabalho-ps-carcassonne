package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlacedTileTest {

    private final TileFeature road = feature(1, FeatureType.ROAD, Direction.NORTH, EdgeLane.CENTER);
    private final TileFeature leftField = feature(2, FeatureType.FIELD, Direction.NORTH, EdgeLane.LEFT);
    private final TileFeature rightField = feature(3, FeatureType.FIELD, Direction.NORTH, EdgeLane.RIGHT);
    private final TileFeature city = feature(4, FeatureType.CITY, Direction.EAST, EdgeLane.CENTER);
    private final Tile tile = new Tile("ASYMMETRIC", List.of(road, leftField, rightField, city));

    @ParameterizedTest
    @EnumSource(Rotation.class)
    void shouldRetainTileAndPlacementAndDelegateGetters(Rotation rotation) {
        Position position = new Position(-2, 3);
        Placement placement = new Placement(position, rotation);

        PlacedTile placed = new PlacedTile(tile, placement);

        assertSame(tile, placed.getTile());
        assertSame(placement, placed.getPlacement());
        assertSame(position, placed.getPosition());
        assertSame(placement.getRotation(), placed.getRotation());
        assertSame(road, placed.feature(1));
        assertEquals("PlacedTile(pos=(-2, 3), rot=" + rotation + ")", placed.toString());
    }

    @Test
    void shouldRejectNullConstructorArguments() {
        Placement placement = new Placement(new Position(0, 0), Rotation.DEG_0);

        assertThrows(NullPointerException.class, () -> new PlacedTile(null, placement));
        assertThrows(NullPointerException.class, () -> new PlacedTile(tile, null));
    }

    @ParameterizedTest
    @CsvSource({
            "DEG_0, NORTH, EAST, SOUTH, WEST",
            "DEG_90, EAST, SOUTH, WEST, NORTH",
            "DEG_180, SOUTH, WEST, NORTH, EAST",
            "DEG_270, WEST, NORTH, EAST, SOUTH"
    })
    void shouldResolveFeaturesAndEdgesForAllRotations(
            Rotation rotation, Direction roadSide, Direction citySide,
            Direction emptySide, Direction otherEmptySide) {
        PlacedTile placed = new PlacedTile(tile, new Placement(new Position(4, -1), rotation));

        assertSame(road, placed.featureAt(roadSide, EdgeLane.CENTER).orElseThrow());
        assertSame(leftField, placed.featureAt(roadSide, EdgeLane.LEFT).orElseThrow());
        assertSame(rightField, placed.featureAt(roadSide, EdgeLane.RIGHT).orElseThrow());
        assertSame(city, placed.featureAt(citySide, EdgeLane.CENTER).orElseThrow());
        assertSame(road, placed.edgeFeature(roadSide).orElseThrow());
        assertSame(city, placed.edgeFeature(citySide).orElseThrow());
        for (Direction side : List.of(emptySide, otherEmptySide)) {
            assertTrue(placed.edgeFeature(side).isEmpty());
            for (EdgeLane lane : EdgeLane.values()) {
                assertTrue(placed.featureAt(side, lane).isEmpty());
            }
        }
        assertTrue(placed.featureAt(citySide, EdgeLane.LEFT).isEmpty());
        assertTrue(placed.featureAt(citySide, EdgeLane.RIGHT).isEmpty());
        assertSame(road, tile.featureAt(Direction.NORTH, EdgeLane.CENTER).orElseThrow());
    }

    @Test
    void shouldUseTileAndPlacementForEqualityAndHashCode() {
        PlacedTile placed = new PlacedTile(tile, new Placement(new Position(2, 3), Rotation.DEG_90));
        PlacedTile equivalent = new PlacedTile(tile, new Placement(new Position(2, 3), Rotation.DEG_90));

        assertEquals(placed, equivalent);
        assertEquals(equivalent, placed);
        assertEquals(placed.hashCode(), equivalent.hashCode());
        assertTrue(Set.of(placed).contains(equivalent));
        assertNotEquals(placed, new PlacedTile(tile, new Placement(new Position(3, 2), Rotation.DEG_90)));
        assertNotEquals(placed, new PlacedTile(tile, new Placement(new Position(2, 3), Rotation.DEG_180)));
        assertNotEquals(placed, new PlacedTile(new Tile("OTHER", List.of()), placed.getPlacement()));
        assertNotEquals(null, placed);
        assertNotEquals(placed.getPlacement(), placed);
    }

    private static TileFeature feature(int id, FeatureType type, Direction side, EdgeLane lane) {
        return new TileFeature(id, type, 0, List.of(new EdgeContact(side, lane)));
    }
}
