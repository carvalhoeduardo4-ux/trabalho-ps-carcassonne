package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.tile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoringRulesTest {

    private ScoringRules scoringRules;

    @BeforeEach
    void setUp() {
        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        scoringRules = new ScoringRules(
                connectivity,
                new RoadScoringStrategy(),
                new CityScoringStrategy(),
                new MonasteryScoringStrategy(),
                new FieldScoringStrategy(connectivity)
        );
    }

    @Test
    void shouldAwardCompletedJunctionRoadToMajorityPlayer() {
        Board board = junctionBoard();

        Player alice =
                new Player("Alice", PlayerColor.RED);

        Player bob =
                new Player("Bob", PlayerColor.BLUE);

        List<MeeplePlacement> meeples = List.of(
                new MeeplePlacement(
                        alice,
                        new FeatureRef(new Position(1, 0), 1)
                ),
                new MeeplePlacement(
                        alice,
                        new FeatureRef(new Position(0, -1), 1)
                ),
                new MeeplePlacement(
                        bob,
                        new FeatureRef(new Position(-1, 0), 1)
                )
        );

        PlacedTile junction =
                board.tileAt(new Position(0, 0))
                        .orElseThrow();

        List<ScoreAward> awards =
                scoringRules.completedAwards(
                        board,
                        meeples,
                        junction
                );

        assertEquals(1, awards.size());

        ScoreAward award = awards.get(0);

        assertEquals(4, award.getPoints());

        assertEquals(
                List.of(alice),
                award.getRecipients()
        );
    }

    @Test
    void tiedPlayersShouldBothReceiveFullScore() {
        Board board = junctionBoard();

        Player alice =
                new Player("Alice", PlayerColor.RED);

        Player bob =
                new Player("Bob", PlayerColor.BLUE);

        List<MeeplePlacement> meeples = List.of(
                new MeeplePlacement(
                        alice,
                        new FeatureRef(new Position(1, 0), 1)
                ),
                new MeeplePlacement(
                        bob,
                        new FeatureRef(new Position(-1, 0), 1)
                )
        );

        PlacedTile junction =
                board.tileAt(new Position(0, 0))
                        .orElseThrow();

        List<ScoreAward> awards =
                scoringRules.completedAwards(
                        board,
                        meeples,
                        junction
                );

        assertEquals(1, awards.size());

        ScoreAward award = awards.get(0);

        assertEquals(4, award.getPoints());

        assertEquals(
                new HashSet<>(List.of(alice, bob)),
                new HashSet<>(award.getRecipients())
        );
    }

    private Board junctionBoard() {
        Board board = new Board();

        Tile junction =
                StandardTiles.createStandardDrawTiles()
                        .stream()
                        .filter(tile -> tile.getId().equals("L"))
                        .findFirst()
                        .orElseThrow();

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
}