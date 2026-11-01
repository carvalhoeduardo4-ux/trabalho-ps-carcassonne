package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.FieldCityAdjacency;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringRulesTest {

    private ScoringRules scoringRules;

    @BeforeEach
    void setUp() {
        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        scoringRules =
                new ScoringRules(
                        connectivity,
                        new RoadScoringStrategy(),
                        new CityScoringStrategy(),
                        new MonasteryScoringStrategy(),
                        new FieldScoringStrategy(
                                connectivity
                        )
                );
    }

    // =====================================================
    // COMPLETED SCORING
    // =====================================================

    @Test
    void shouldAwardCompletedJunctionRoadToMajorityPlayer() {
        Board board =
                junctionBoard();

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        Player bob =
                new Player(
                        "Bob",
                        PlayerColor.BLUE
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(1, 0),
                                        1
                                )
                        ),
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(0, -1),
                                        1
                                )
                        ),
                        new MeeplePlacement(
                                bob,
                                new FeatureRef(
                                        new Position(-1, 0),
                                        1
                                )
                        )
                );

        PlacedTile junction =
                board.tileAt(
                        new Position(0, 0)
                ).orElseThrow();

        List<ScoreAward> awards =
                scoringRules.completedAwards(
                        board,
                        meeples,
                        junction
                );

        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        assertEquals(
                4,
                award.getPoints()
        );

        assertEquals(
                List.of(alice),
                award.getRecipients()
        );
    }

    @Test
    void tiedPlayersShouldBothReceiveFullScore() {
        Board board =
                junctionBoard();

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        Player bob =
                new Player(
                        "Bob",
                        PlayerColor.BLUE
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(1, 0),
                                        1
                                )
                        ),
                        new MeeplePlacement(
                                bob,
                                new FeatureRef(
                                        new Position(-1, 0),
                                        1
                                )
                        )
                );

        PlacedTile junction =
                board.tileAt(
                        new Position(0, 0)
                ).orElseThrow();

        List<ScoreAward> awards =
                scoringRules.completedAwards(
                        board,
                        meeples,
                        junction
                );

        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        assertEquals(
                4,
                award.getPoints()
        );

        assertEquals(
                new HashSet<>(
                        List.of(
                                alice,
                                bob
                        )
                ),
                new HashSet<>(
                        award.getRecipients()
                )
        );
    }

    // =====================================================
    // END GAME SCORING
    // =====================================================

    @Test
    void endGameShouldScoreIncompleteCityWithShield() {
        Board board =
                new Board();

        TileFeature city =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        1,
                        List.of(
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.CENTER
                                )
                        )
                );

        Tile tile =
                new Tile(
                        "CITY",
                        List.of(city)
                );

        board.place(
                new PlacedTile(
                        tile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        )
                );

        List<ScoreAward> awards =
                scoringRules.endGameAwards(
                        board,
                        meeples
                );

        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        /*
         * Cidade incompleta:
         *
         * 1 tile + 1 shield = 2.
         */
        assertEquals(
                2,
                award.getPoints()
        );

        assertEquals(
                List.of(alice),
                award.getRecipients()
        );
    }

    @Test
    void endGameShouldScoreConnectedRegionOnlyOnceAndPreserveTie() {
        Board board =
                new Board();

        Tile road =
                new Tile(
                        "ROAD",
                        List.of(
                                new TileFeature(
                                        1,
                                        FeatureType.ROAD,
                                        0,
                                        List.of(
                                                new EdgeContact(
                                                        Direction.WEST,
                                                        EdgeLane.CENTER
                                                ),
                                                new EdgeContact(
                                                        Direction.EAST,
                                                        EdgeLane.CENTER
                                                )
                                        )
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        road,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        board.place(
                new PlacedTile(
                        road,
                        new Position(1, 0),
                        Rotation.DEG_0
                )
        );

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        Player bob =
                new Player(
                        "Bob",
                        PlayerColor.BLUE
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        ),
                        new MeeplePlacement(
                                bob,
                                new FeatureRef(
                                        new Position(1, 0),
                                        1
                                )
                        )
                );

        List<ScoreAward> awards =
                scoringRules.endGameAwards(
                        board,
                        meeples
                );

        /*
         * Existem dois meeples, mas somente
         * uma região conectada.
         */
        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        /*
         * Estrada incompleta com duas peças.
         */
        assertEquals(
                2,
                award.getPoints()
        );

        assertEquals(
                new HashSet<>(
                        List.of(
                                alice,
                                bob
                        )
                ),
                new HashSet<>(
                        award.getRecipients()
                )
        );
    }

    @Test
    void endGameShouldScoreFieldFromCompletedAdjacentCity() {
        Board board =
                new Board();

        TileFeature city =
                new TileFeature(
                        1,
                        FeatureType.CITY,
                        0,
                        List.of()
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
                        "FIELD",
                        List.of(
                                city,
                                field
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        field,
                                        city
                                )
                        )
                );

        board.place(
                new PlacedTile(
                        tile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(0, 0),
                                        2
                                )
                        )
                );

        List<ScoreAward> awards =
                scoringRules.endGameAwards(
                        board,
                        meeples
                );

        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        /*
         * Uma cidade completa adjacente:
         *
         * 1 * 3 = 3.
         */
        assertEquals(
                3,
                award.getPoints()
        );

        assertEquals(
                List.of(alice),
                award.getRecipients()
        );
    }

    @Test
    void endGameShouldKeepZeroPointFieldAwardSoMeepleCanBeReturned() {
        Board board =
                new Board();

        TileFeature field =
                new TileFeature(
                        1,
                        FeatureType.FIELD,
                        0,
                        List.of()
                );

        Tile tile =
                new Tile(
                        "EMPTY-FIELD",
                        List.of(field)
                );

        board.place(
                new PlacedTile(
                        tile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        Player alice =
                new Player(
                        "Alice",
                        PlayerColor.RED
                );

        List<MeeplePlacement> meeples =
                List.of(
                        new MeeplePlacement(
                                alice,
                                new FeatureRef(
                                        new Position(0, 0),
                                        1
                                )
                        )
                );

        List<ScoreAward> awards =
                scoringRules.endGameAwards(
                        board,
                        meeples
                );

        /*
         * O campo não vale pontos, mas ainda precisa
         * gerar um ScoreAward para que o Game possa
         * devolver o meeple da região.
         */
        assertEquals(
                1,
                awards.size()
        );

        ScoreAward award =
                awards.get(0);

        assertEquals(
                0,
                award.getPoints()
        );

        assertEquals(
                List.of(alice),
                award.getRecipients()
        );

        assertEquals(
                FeatureType.FIELD,
                award.getRegion().getType()
        );

        assertTrue(
                award.getRegion().contains(
                        new FeatureRef(
                                new Position(0, 0),
                                1
                        )
                )
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Board junctionBoard() {
        Board board =
                new Board();

        Tile junction =
                StandardTiles
                        .createStandardDrawTiles()
                        .stream()
                        .filter(
                                tile ->
                                        tile.getId()
                                                .equals("L")
                        )
                        .findFirst()
                        .orElseThrow();

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
}