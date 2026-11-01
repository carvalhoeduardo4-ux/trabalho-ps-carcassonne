package com.carcassonne.domain.tile;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardTilesTest {

    @Test
    void tileLShouldModelRoadJunctionAsSingleFeature() {
        List<Tile> tiles = tilesById("L");

        assertEquals(3, tiles.size());

        for (Tile tile : tiles) {
            List<TileFeature> roads = roadFeatures(tile);

            assertEquals(1, roads.size());

            TileFeature road = roads.get(0);

            assertEquals(2, road.getId());

            assertRoadContacts(
                    road,
                    Direction.EAST,
                    Direction.SOUTH,
                    Direction.WEST
            );
        }
    }

    @Test
    void tileWShouldModelRoadJunctionAsSingleFeature() {
        List<Tile> tiles = tilesById("W");

        assertEquals(4, tiles.size());

        for (Tile tile : tiles) {
            List<TileFeature> roads = roadFeatures(tile);

            assertEquals(1, roads.size());

            TileFeature road = roads.get(0);

            assertEquals(1, road.getId());

            assertRoadContacts(
                    road,
                    Direction.SOUTH,
                    Direction.WEST,
                    Direction.EAST
            );
        }
    }

    @Test
    void tileXShouldModelCrossroadAsSingleFeature() {
        List<Tile> tiles = tilesById("X");

        assertEquals(1, tiles.size());

        Tile tile = tiles.get(0);

        List<TileFeature> roads = roadFeatures(tile);

        assertEquals(1, roads.size());

        TileFeature road = roads.get(0);

        assertEquals(1, road.getId());

        assertRoadContacts(
                road,
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        );
    }

    @Test
    void standardDrawDeckShouldStillContain71Tiles() {
        assertEquals(
                71,
                StandardTiles.createStandardDrawTiles().size()
        );
    }

    private List<Tile> tilesById(String id) {
        return StandardTiles.createStandardDrawTiles()
                .stream()
                .filter(tile -> tile.getId().equals(id))
                .toList();
    }

    private List<TileFeature> roadFeatures(Tile tile) {
        return tile.getFeatures()
                .stream()
                .filter(feature -> feature.getType() == FeatureType.ROAD)
                .toList();
    }

    private void assertRoadContacts(
            TileFeature road,
            Direction... directions
    ) {
        Set<Direction> actualDirections =
                road.getEdgeContacts()
                        .stream()
                        .map(EdgeContact::getSide)
                        .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                Set.of(directions),
                actualDirections
        );

        assertEquals(
                directions.length,
                road.getEdgeContacts().size()
        );

        assertTrue(
                road.getEdgeContacts()
                        .stream()
                        .allMatch(
                                contact ->
                                        contact.getLane()
                                                == EdgeLane.CENTER
                        )
        );
    }
}