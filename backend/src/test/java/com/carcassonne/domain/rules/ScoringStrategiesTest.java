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
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoringStrategiesTest {

    @Test
    void roadShouldScoreOnePointPerDistinctTile() {
        RoadScoringStrategy strategy =
                new RoadScoringStrategy();

        FeatureRegion region =
                new FeatureRegion(
                        FeatureType.ROAD,
                        List.of(
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                ),
                                new FeatureRef(
                                        new Position(1, 0),
                                        1
                                ),
                                new FeatureRef(
                                        new Position(2, 0),
                                        1
                                )
                        )
                );

        Board board =
                new Board();

        assertEquals(
                3,
                strategy.completedScore(
                        board,
                        region
                )
        );

        assertEquals(
                3,
                strategy.endGameScore(
                        board,
                        region
                )
        );
    }

    @Test
    void completedCityShouldScoreTwoPerTileAndTwoPerShield() {
        Board board =
                new Board();

        board.place(
                cityTile(
                        "C1",
                        new Position(0, 0),
                        1
                )
        );

        board.place(
                cityTile(
                        "C2",
                        new Position(1, 0),
                        0
                )
        );

        FeatureRegion region =
                new FeatureRegion(
                        FeatureType.CITY,
                        List.of(
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                ),
                                new FeatureRef(
                                        new Position(1, 0),
                                        1
                                )
                        )
                );

        CityScoringStrategy strategy =
                new CityScoringStrategy();

        /*
         * 2 tiles + 1 shield
         *
         * completed:
         * 2 * (2 + 1) = 6
         *
         * end game:
         * 2 + 1 = 3
         */
        assertEquals(
                6,
                strategy.completedScore(
                        board,
                        region
                )
        );

        assertEquals(
                3,
                strategy.endGameScore(
                        board,
                        region
                )
        );
    }

    @Test
    void completedMonasteryShouldScoreNinePoints() {
        Board board =
                monasteryBoard(3);

        FeatureRegion region =
                new FeatureRegion(
                        FeatureType.MONASTERY,
                        List.of(
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        )
                );

        MonasteryScoringStrategy strategy =
                new MonasteryScoringStrategy();

        assertEquals(
                9,
                strategy.completedScore(
                        board,
                        region
                )
        );
    }

    @Test
    void incompleteMonasteryShouldScoreCenterPlusSurroundingTiles() {
        Board board =
                monasteryBoard(3);

        FeatureRegion region =
                new FeatureRegion(
                        FeatureType.MONASTERY,
                        List.of(
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        )
                );

        MonasteryScoringStrategy strategy =
                new MonasteryScoringStrategy();

        assertEquals(
                4,
                strategy.endGameScore(
                        board,
                        region
                )
        );
    }

    @Test
    void fieldShouldScoreZeroDuringTurn() {
        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        FieldScoringStrategy strategy =
                new FieldScoringStrategy(
                        connectivity
                );

        FeatureRegion field =
                new FeatureRegion(
                        FeatureType.FIELD,
                        List.of(
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        )
                );

        assertEquals(
                0,
                strategy.completedScore(
                        new Board(),
                        field
                )
        );
    }

    @Test
    void fieldShouldScoreThreePointsPerCompleteAdjacentCity() {
        Board board =
                new Board();

        Position position =
                new Position(0, 0);

        /*
         * CITY #1
         *
         * Sem contatos de borda:
         * cidade completa dentro da própria peça.
         */
        TileFeature completeCityOne =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        0,
                        List.of()
                );

        /*
         * CITY #2
         *
         * Outra cidade completa e distinta.
         */
        TileFeature completeCityTwo =
                new TileFeature(
                        2,
                        FeatureType.CITY,
                        0,
                        List.of()
                );

        /*
         * CITY #3
         *
         * Possui uma ponta aberta para EAST,
         * então está incompleta.
         */
        TileFeature incompleteCity =
                new TileFeature(
                        3,
                        FeatureType.CITY,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.CENTER
                                )
                        )
                );

        TileFeature field =
                new TileFeature(
                        4,
                        FeatureType.FIELD,
                        0,
                        List.of()
                );

        Tile tile =
                new Tile(
                        "FIELD-SCORING",
                        List.of(
                                completeCityOne,
                                completeCityTwo,
                                incompleteCity,
                                field
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        field,
                                        completeCityOne
                                ),
                                new FieldCityAdjacency(
                                        field,
                                        completeCityTwo
                                ),
                                new FieldCityAdjacency(
                                        field,
                                        incompleteCity
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        tile,
                        position,
                        Rotation.DEG_0
                )
        );

        FeatureRegion fieldRegion =
                new FeatureRegion(
                        FeatureType.FIELD,
                        List.of(
                                new FeatureRef(
                                        position,
                                        4
                                )
                        )
                );

        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        FieldScoringStrategy strategy =
                new FieldScoringStrategy(
                        connectivity
                );

        /*
         * Duas cidades completas:
         *
         * 2 * 3 = 6 pontos.
         *
         * A terceira cidade é incompleta
         * e não deve pontuar.
         */
        assertEquals(
                6,
                strategy.endGameScore(
                        board,
                        fieldRegion
                )
        );
    }

    @Test
    void fieldShouldScoreZeroWhenNoAdjacentCityIsComplete() {
        Board board =
                new Board();

        Position position =
                new Position(0, 0);

        TileFeature incompleteCity =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.NORTH,
                                        EdgeLane.CENTER
                                )
                        )
                );

        TileFeature field =
                new TileFeature(
                        2,
                        FeatureType.FIELD,
                        0,
                        List.of()
                );

        Tile tile =
                new Tile(
                        "INCOMPLETE-CITY",
                        List.of(
                                incompleteCity,
                                field
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        field,
                                        incompleteCity
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        tile,
                        position,
                        Rotation.DEG_0
                )
        );

        FeatureRegion fieldRegion =
                new FeatureRegion(
                        FeatureType.FIELD,
                        List.of(
                                new FeatureRef(
                                        position,
                                        2
                                )
                        )
                );

        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        FieldScoringStrategy strategy =
                new FieldScoringStrategy(
                        connectivity
                );

        assertEquals(
                0,
                strategy.endGameScore(
                        board,
                        fieldRegion
                )
        );
    }

    private PlacedTile cityTile(
            String id,
            Position position,
            int shields
    ) {
        Tile tile =
                new Tile(
                        id,
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.CITY,
                                        shields,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.EAST,
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

    private Board monasteryBoard(
            int surroundingTiles
    ) {
        Board board =
                new Board();

        Tile monastery =
                new Tile(
                        "M",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.MONASTERY,
                                        0,
                                        List.of()
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        monastery,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        List<Position> positions =
                List.of(
                        new Position(1, 0),
                        new Position(0, 1),
                        new Position(-1, 0),
                        new Position(0, -1),
                        new Position(1, 1),
                        new Position(-1, 1),
                        new Position(1, -1),
                        new Position(-1, -1)
                );

        for (int i = 0;
             i < surroundingTiles;
             i++) {

            board.place(
                    new PlacedTile(
                            dummyTile(
                                    "D" + i
                            ),
                            positions.get(i),
                            Rotation.DEG_0
                    )
            );
        }

        return board;
    }

    private Tile dummyTile(
            String id
    ) {
        return new Tile(
                id,
                List.of(
                        new TileFeature(
                                1,
                                FeatureType.FIELD,
                                0,
                                List.of()
                        )
                )
        );
    }
}