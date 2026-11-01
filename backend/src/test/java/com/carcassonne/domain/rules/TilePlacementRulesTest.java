package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TilePlacementRulesTest {

    private final TilePlacementRules rules =
            new TilePlacementRules();

    @Test
    void shouldAllowPlacementWhenCenterEdgeTypesMatch() {
        Board board =
                new Board();

        /*
         * Peça existente:
         *
         * EAST:
         *
         * FIELD | ROAD | FIELD
         */
        Tile existing =
                new Tile(
                        "EXISTING",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.ROAD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.EAST,
                                                        EdgeLane.CENTER
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
                                                        EdgeLane.LEFT
                                                )
                                        )
                                ),
                                new TileFeature(
                                        3,
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

        board.place(
                new PlacedTile(
                        existing,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        /*
         * Nova peça:
         *
         * WEST:
         *
         * FIELD | ROAD | FIELD
         *
         * Os campos possuem IDs e agrupamentos diferentes,
         * mas isso não interfere na legalidade do encaixe.
         */
        Tile candidate =
                new Tile(
                        "CANDIDATE",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.ROAD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.WEST,
                                                        EdgeLane.CENTER
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
                                                        EdgeLane.LEFT
                                                ),
                                                new EdgeContact(
                                                        Direction.WEST,
                                                        EdgeLane.RIGHT
                                                )
                                        )
                                )
                        )
                );

        assertTrue(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(1, 0),
                                Rotation.DEG_0
                        )
                )
        );
    }

    @Test
    void shouldRejectPlacementWhenCenterEdgeTypesDiffer() {
        Board board =
                new Board();

        Tile existing =
                tileWithCenterEdge(
                        "ROAD",
                        Direction.EAST,
                        FeatureType.ROAD
                );

        board.place(
                new PlacedTile(
                        existing,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        Tile candidate =
                tileWithCenterEdge(
                        "FIELD",
                        Direction.WEST,
                        FeatureType.FIELD
                );

        assertFalse(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(1, 0),
                                Rotation.DEG_0
                        )
                )
        );
    }

    @Test
    void shouldConsiderRotationWhenMatchingCenterLane() {
        Board board =
                new Board();

        Tile existing =
                tileWithCenterEdge(
                        "EXISTING",
                        Direction.EAST,
                        FeatureType.ROAD
                );

        board.place(
                new PlacedTile(
                        existing,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        /*
         * A estrada desta peça está originalmente ao NORTH.
         *
         * Com DEG_270:
         *
         * NORTH -> WEST
         *
         * portanto passa a tocar a borda EAST
         * da peça existente.
         */
        Tile candidate =
                new Tile(
                        "ROTATED",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.ROAD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.NORTH,
                                                        EdgeLane.CENTER
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
                                                        EdgeLane.CENTER
                                                )
                                        )
                                )
                        )
                );

        /*
         * Sem rotação, WEST é FIELD.
         */
        assertFalse(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(1, 0),
                                Rotation.DEG_0
                        )
                )
        );

        /*
         * Com 270 graus, NORTH passa para WEST.
         */
        assertTrue(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(1, 0),
                                Rotation.DEG_270
                        )
                )
        );
    }

    @Test
    void shouldRejectPlacementWithoutAdjacentTile() {
        Board board =
                new Board();

        Tile candidate =
                tileWithCenterEdge(
                        "ROAD",
                        Direction.WEST,
                        FeatureType.ROAD
                );

        assertFalse(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(5, 5),
                                Rotation.DEG_0
                        )
                )
        );
    }

    @Test
    void shouldRejectPlacementOnOccupiedPosition() {
        Board board =
                new Board();

        Tile existing =
                tileWithCenterEdge(
                        "EXISTING",
                        Direction.EAST,
                        FeatureType.ROAD
                );

        board.place(
                new PlacedTile(
                        existing,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        Tile candidate =
                tileWithCenterEdge(
                        "CANDIDATE",
                        Direction.WEST,
                        FeatureType.ROAD
                );

        assertFalse(
                rules.canPlace(
                        board,
                        candidate,
                        new Placement(
                                new Position(0, 0),
                                Rotation.DEG_0
                        )
                )
        );
    }

    private Tile tileWithCenterEdge(
            String id,
            Direction direction,
            FeatureType type
    ) {
        return new Tile(
                id,
                List.of(
                        new TileFeature(
                                1,
                                type,
                                0,
                                List.of(
                                        new EdgeContact(
                                                direction,
                                                EdgeLane.CENTER
                                        )
                                )
                        )
                )
        );
    }
}