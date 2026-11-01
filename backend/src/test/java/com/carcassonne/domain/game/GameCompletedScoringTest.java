package com.carcassonne.domain.game;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.rules.CityScoringStrategy;
import com.carcassonne.domain.rules.FeatureConnectivity;
import com.carcassonne.domain.rules.FieldScoringStrategy;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.MonasteryScoringStrategy;
import com.carcassonne.domain.rules.RoadScoringStrategy;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameCompletedScoringTest {

    @Test
    void scoreCompletedFeaturesShouldAwardPointsAndReturnMeeple() {
        Player alice =
                new Player("Alice", PlayerColor.RED);

        Player bob =
                new Player("Bob", PlayerColor.BLUE);

        Board board = new Board();

        /*
         * Primeira metade da estrada:
         *
         * (0,0) ---->
         */
        Tile leftRoadEnd = roadEndTile(
                "LEFT",
                Direction.EAST
        );

        board.place(
                new PlacedTile(
                        leftRoadEnd,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        /*
         * Segunda metade será comprada e colocada em (1,0):
         *
         * <---- (1,0)
         *
         * Após a colocação:
         *
         * (0,0) <------> (1,0)
         *
         * estrada completa com 2 peças.
         */
        Tile rightRoadEnd = roadEndTile(
                "RIGHT",
                Direction.WEST
        );

        TileDeck deck =
                new TileDeck(
                        List.of(rightRoadEnd)
                );

        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        TilePlacementRules tileRules =
                new TilePlacementRules();

        MeeplePlacementRules meepleRules =
                new MeeplePlacementRules(connectivity);

        ScoringRules scoringRules =
                new ScoringRules(
                        connectivity,
                        new RoadScoringStrategy(),
                        new CityScoringStrategy(),
                        new MonasteryScoringStrategy(),
                        new FieldScoringStrategy(connectivity)
                );

        Game game = new Game(
                board,
                deck,
                List.of(alice, bob),
                new Turn(),
                tileRules,
                meepleRules,
                scoringRules
        );

        assertEquals(
                7,
                game.availableMeeples(alice)
        );

        game.drawTile();

        game.placeTile(
                new Placement(
                        new Position(1, 0),
                        Rotation.DEG_0
                )
        );

        game.placeMeeple(
                new FeatureRef(
                        new Position(1, 0),
                        1
                )
        );

        assertEquals(
                6,
                game.availableMeeples(alice)
        );

        assertEquals(
                1,
                game.getMeeples().size()
        );

        game.scoreCompletedFeatures();

        /*
         * Estrada completa com 2 peças.
         */
        assertEquals(
                2,
                alice.getScore()
        );

        assertEquals(
                0,
                bob.getScore()
        );

        /*
         * O meeple da região pontuada deve ser devolvido.
         */
        assertEquals(
                7,
                game.availableMeeples(alice)
        );

        assertTrue(
                game.getMeeples().isEmpty()
        );
    }

    private Tile roadEndTile(
            String id,
            Direction direction
    ) {
        return new Tile(
                id,
                List.of(
                        new TileFeature(
                                1,
                                FeatureType.ROAD,
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