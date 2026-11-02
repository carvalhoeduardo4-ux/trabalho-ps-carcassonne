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
import com.carcassonne.domain.tile.FieldCityAdjacency;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEndGameScoringTest {

    @Test
    void scoreEndGameShouldAwardIncompleteRoadAndReturnMeeple() {
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

        Board board =
                new Board();

        Tile initialRoad =
                roadEndTile(
                        "INITIAL",
                        Direction.EAST
                );

        board.place(
                new PlacedTile(
                        initialRoad,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        /*
         * A segunda peça conecta pela esquerda,
         * mas continua aberta pela direita:
         *
         * (0,0) -------- (1,0) ---->
         *
         * Portanto a estrada está incompleta no
         * fim da partida e vale 2 pontos.
         */
        Tile openRoad =
                horizontalRoadTile("OPEN");

        Game game =
                newGame(
                        board,
                        new TileDeck(
                                List.of(openRoad)
                        ),
                        List.of(
                                alice,
                                bob
                        )
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

        game.scoreEndGame();

        assertEquals(
                2,
                alice.getScore()
        );

        assertEquals(
                0,
                bob.getScore()
        );

        assertEquals(
                7,
                game.availableMeeples(alice)
        );

        assertTrue(
                game.getMeeples().isEmpty()
        );
    }

    @Test
    void scoreEndGameShouldAwardFieldAndReturnFarmer() {
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

        Board board =
                new Board();

        TileFeature initialField =
                new TileFeature(
                        1,
                        FeatureType.FIELD,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.LEFT
                                ),
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.CENTER
                                ),
                                new EdgeContact(
                                        Direction.EAST,
                                        EdgeLane.RIGHT
                                )
                        )
                );

        Tile initialTile =
                new Tile(
                        "INITIAL-FIELD",
                        List.of(initialField)
                );

        board.place(
                new PlacedTile(
                        initialTile,
                        new Position(0, 0),
                        Rotation.DEG_0
                )
        );

        TileFeature field =
                new TileFeature(
                        1,
                        FeatureType.FIELD,
                        0,
                        List.of(
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.LEFT
                                ),
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.CENTER
                                ),
                                new EdgeContact(
                                        Direction.WEST,
                                        EdgeLane.RIGHT
                                )
                        )
                );

        /*
         * Cidade sem contatos de borda:
         * é uma cidade completa dentro da própria peça.
         */
        TileFeature city =
                new TileFeature(
                        2,
                        FeatureType.CITY,
                        0,
                        List.of()
                );

        Tile fieldWithCity =
                new Tile(
                        "FIELD-WITH-CITY",
                        List.of(
                                field,
                                city
                        ),
                        List.of(
                                new FieldCityAdjacency(
                                        field,
                                        city
                                )
                        )
                );

        Game game =
                newGame(
                        board,
                        new TileDeck(
                                List.of(fieldWithCity)
                        ),
                        List.of(
                                alice,
                                bob
                        )
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

        game.scoreEndGame();

        /*
         * Uma cidade completa adjacente ao campo:
         * 1 * 3 = 3 pontos.
         */
        assertEquals(
                3,
                alice.getScore()
        );

        assertEquals(
                0,
                bob.getScore()
        );

        assertEquals(
                7,
                game.availableMeeples(alice)
        );

        assertTrue(
                game.getMeeples().isEmpty()
        );
    }

    private Game newGame(
            Board board,
            TileDeck deck,
            List<Player> players
    ) {
        FeatureConnectivity connectivity =
                new FeatureConnectivity();

        TilePlacementRules tileRules =
                new TilePlacementRules();

        MeeplePlacementRules meepleRules =
                new MeeplePlacementRules(
                        connectivity
                );

        ScoringRules scoringRules =
                new ScoringRules(
                        connectivity,
                        new RoadScoringStrategy(),
                        new CityScoringStrategy(),
                        new MonasteryScoringStrategy(),
                        new FieldScoringStrategy(
                                connectivity
                        )
                );

        return new Game(
                board,
                deck,
                players,
                new Turn(),
                tileRules,
                meepleRules,
                scoringRules
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

    private Tile horizontalRoadTile(
            String id
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
    }
}
