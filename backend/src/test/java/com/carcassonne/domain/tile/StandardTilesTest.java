package com.carcassonne.domain.tile;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
            assertRoadContacts(road, Direction.EAST, Direction.SOUTH, Direction.WEST);
            assertEquals(3, fieldFeatures(tile).size());
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
            assertRoadContacts(road, Direction.SOUTH, Direction.WEST, Direction.EAST);
            assertEquals(3, fieldFeatures(tile).size());
            assertEquals(FeatureType.FIELD, tile.feature(4).getType());
        }
    }

    @Test
    void tileXShouldModelCrossroadAsSingleFeatureAndFourFields() {
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
        assertEquals(4, fieldFeatures(tile).size());
    }

    @Test
    void tileUShouldSplitFieldsAcrossStraightRoad() {
        List<Tile> tiles = tilesById("U");
        assertEquals(8, tiles.size());

        for (Tile tile : tiles) {
            List<TileFeature> fields = fieldFeatures(tile);
            assertEquals(2, fields.size());

            TileFeature eastField = tile.feature(2);
            TileFeature westField = tile.feature(3);

            assertEquals(FeatureType.FIELD, eastField.getType());
            assertEquals(FeatureType.FIELD, westField.getType());

            assertTrue(eastField.getEdgeContacts().contains(
                    new EdgeContact(Direction.NORTH, EdgeLane.RIGHT)
            ));
            assertTrue(eastField.getEdgeContacts().contains(
                    new EdgeContact(Direction.SOUTH, EdgeLane.LEFT)
            ));
            assertTrue(westField.getEdgeContacts().contains(
                    new EdgeContact(Direction.NORTH, EdgeLane.LEFT)
            ));
            assertTrue(westField.getEdgeContacts().contains(
                    new EdgeContact(Direction.SOUTH, EdgeLane.RIGHT)
            ));
        }
    }

    @Test
    void tileFShouldSplitNorthAndSouthFieldsAndLinkBothToCity() {
        List<Tile> tiles = tilesById("F");
        assertEquals(2, tiles.size());

        for (Tile tile : tiles) {
            assertEquals(2, fieldFeatures(tile).size());
            assertEquals(2, tile.getFieldCityAdjacencies().size());

            TileFeature city = tile.feature(1);
            TileFeature northField = tile.feature(2);
            TileFeature southField = tile.feature(3);

            assertAdjacency(tile, northField, city);
            assertAdjacency(tile, southField, city);
        }
    }

    @Test
    void tileGShouldSplitEastAndWestFieldsAndLinkBothToCity() {
        Tile tile = tilesById("G").get(0);

        assertEquals(2, fieldFeatures(tile).size());
        assertEquals(2, tile.getFieldCityAdjacencies().size());

        TileFeature city = tile.feature(1);
        TileFeature eastField = tile.feature(2);
        TileFeature westField = tile.feature(3);

        assertAdjacency(tile, eastField, city);
        assertAdjacency(tile, westField, city);
    }

    @Test
    void starterTileShouldExposeBothFieldsSeparatedByRoad() {
        Tile starter = StandardTiles.createStarterTile();

        assertEquals("START", starter.getId());
        assertEquals(2, fieldFeatures(starter).size());
        assertEquals(1, starter.getFieldCityAdjacencies().size());

        assertAdjacency(
                starter,
                starter.feature(4),
                starter.feature(1)
        );
    }

    @Test
    void standardTilesShouldExposeConsistentLaneTopologyOnEveryEdge() {
        for (Tile tile : allStandardTiles()) {
            for (Direction side : Direction.values()) {
                TileFeature center = tile.featureAt(side, EdgeLane.CENTER)
                        .orElseThrow(() -> new AssertionError(
                                "Missing CENTER on " + tile.getId() + " " + side
                        ));

                if (center.getType() == FeatureType.ROAD) {
                    TileFeature left = tile.featureAt(side, EdgeLane.LEFT)
                            .orElseThrow(() -> new AssertionError(
                                    "Missing LEFT field beside road on "
                                            + tile.getId() + " " + side
                            ));

                    TileFeature right = tile.featureAt(side, EdgeLane.RIGHT)
                            .orElseThrow(() -> new AssertionError(
                                    "Missing RIGHT field beside road on "
                                            + tile.getId() + " " + side
                            ));

                    assertEquals(FeatureType.FIELD, left.getType());
                    assertEquals(FeatureType.FIELD, right.getType());
                }

                if (center.getType() == FeatureType.FIELD) {
                    TileFeature left = tile.featureAt(side, EdgeLane.LEFT)
                            .orElseThrow();
                    TileFeature right = tile.featureAt(side, EdgeLane.RIGHT)
                            .orElseThrow();

                    assertSame(center, left);
                    assertSame(center, right);
                }
            }
        }
    }

    @Test
    void fieldCityAdjacenciesShouldReferenceFeaturesFromSameTile() {
        for (Tile tile : allStandardTiles()) {
            for (FieldCityAdjacency adjacency : tile.getFieldCityAdjacencies()) {
                assertTrue(tile.getFeatures().contains(adjacency.getField()));
                assertTrue(tile.getFeatures().contains(adjacency.getCity()));
                assertEquals(FeatureType.FIELD, adjacency.getField().getType());
                assertEquals(FeatureType.CITY, adjacency.getCity().getType());
            }
        }
    }

    @Test
    void fieldRegionCountsShouldMatchStandardTileTopology() {
        Map<String, Integer> expected = Map.ofEntries(
                Map.entry("START", 2),
                Map.entry("A", 1),
                Map.entry("B", 1),
                Map.entry("C", 0),
                Map.entry("D", 2),
                Map.entry("E", 1),
                Map.entry("F", 2),
                Map.entry("G", 2),
                Map.entry("H", 1),
                Map.entry("I", 1),
                Map.entry("J", 2),
                Map.entry("K", 2),
                Map.entry("L", 3),
                Map.entry("M", 1),
                Map.entry("N", 1),
                Map.entry("O", 2),
                Map.entry("P", 2),
                Map.entry("Q", 1),
                Map.entry("R", 1),
                Map.entry("S", 2),
                Map.entry("T", 2),
                Map.entry("U", 2),
                Map.entry("V", 2),
                Map.entry("W", 3),
                Map.entry("X", 4)
        );

        for (Tile tile : allStandardTiles()) {
            assertEquals(
                    expected.get(tile.getId()),
                    fieldFeatures(tile).size(),
                    "Unexpected field count for tile " + tile.getId()
            );
        }
    }

    @Test
    void standardDrawDeckShouldStillContain71Tiles() {
        assertEquals(
                71,
                StandardTiles.createStandardDrawTiles().size()
        );
    }

    private List<Tile> allStandardTiles() {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(StandardTiles.createStarterTile());
        tiles.addAll(StandardTiles.createStandardDrawTiles());
        return tiles;
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

    private List<TileFeature> fieldFeatures(Tile tile) {
        return tile.getFeatures()
                .stream()
                .filter(feature -> feature.getType() == FeatureType.FIELD)
                .toList();
    }

    private void assertAdjacency(
            Tile tile,
            TileFeature field,
            TileFeature city
    ) {
        assertTrue(
                tile.getFieldCityAdjacencies()
                        .contains(new FieldCityAdjacency(field, city))
        );
    }

    private void assertRoadContacts(
            TileFeature road,
            Direction... directions
    ) {
        Set<Direction> actualDirections = road.getEdgeContacts()
                .stream()
                .map(EdgeContact::getSide)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of(directions), actualDirections);
        assertEquals(directions.length, road.getEdgeContacts().size());
        assertTrue(
                road.getEdgeContacts()
                        .stream()
                        .allMatch(contact -> contact.getLane() == EdgeLane.CENTER)
        );
    }
}
