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
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeeplePlacementRulesTest {

    private final FeatureConnectivity connectivity =
            new FeatureConnectivity();

    private final MeeplePlacementRules rules =
            new MeeplePlacementRules(connectivity);

    @Test
    void shouldRejectMeepleWhenConnectedRoadAlreadyContainsMeeple() {
        Board board = junctionBoard();

        Player player =
                new Player("Alice", PlayerColor.RED);

        List<MeeplePlacement> meeples = List.of(
                new MeeplePlacement(
                        player,
                        new FeatureRef(
                                new Position(1, 0),
                                1
                        )
                )
        );

        FeatureRef junctionRoad =
                new FeatureRef(new Position(0, 0), 2);

        assertFalse(
                rules.canPlace(
                        board,
                        meeples,
                        junctionRoad
                )
        );
    }

    @Test
    void shouldAllowMeepleWhenConnectedRoadIsEmpty() {
        Board board = junctionBoard();

        FeatureRef junctionRoad =
                new FeatureRef(new Position(0, 0), 2);

        assertTrue(
                rules.canPlace(
                        board,
                        List.of(),
                        junctionRoad
                )
        );
    }

    private Board junctionBoard() {
        Board board = new Board();

        Tile junction = StandardTiles.createStandardDrawTiles()
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