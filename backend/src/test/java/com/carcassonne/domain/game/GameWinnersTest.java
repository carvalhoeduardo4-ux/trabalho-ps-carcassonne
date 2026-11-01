package com.carcassonne.domain.game;

import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameWinnersTest {

    @Test
    void returnsSinglePlayerWithHighestScore() {
        Player alice = new Player("Alice", PlayerColor.RED, 30);
        Player bob = new Player("Bob", PlayerColor.BLUE, 20);

        Game game = new Game(List.of(alice, bob));
        game.finish();

        List<Player> winners = game.winners();

        assertEquals(List.of(alice), winners);
    }

    @Test
    void returnsAllPlayersTiedForHighestScore() {
        Player alice = new Player("Alice", PlayerColor.RED, 30);
        Player bob = new Player("Bob", PlayerColor.BLUE, 30);
        Player carol = new Player("Carol", PlayerColor.GREEN, 15);

        Game game = new Game(List.of(alice, bob, carol));
        game.finish();

        List<Player> winners = game.winners();

        assertEquals(List.of(alice, bob), winners);
    }

    @Test
    void returnsAllPlayersWhenEveryScoreIsEqual() {
        Player alice = new Player("Alice", PlayerColor.RED);
        Player bob = new Player("Bob", PlayerColor.BLUE);
        Player carol = new Player("Carol", PlayerColor.GREEN);

        Game game = new Game(List.of(alice, bob, carol));
        game.finish();

        List<Player> winners = game.winners();

        assertEquals(List.of(alice, bob, carol), winners);
    }
}