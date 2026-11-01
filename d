[1mdiff --git a/backend/src/main/java/com/carcassonne/domain/game/Game.java b/backend/src/main/java/com/carcassonne/domain/game/Game.java[m
[1mindex c1247d6..64062f0 100644[m
[1m--- a/backend/src/main/java/com/carcassonne/domain/game/Game.java[m
[1m+++ b/backend/src/main/java/com/carcassonne/domain/game/Game.java[m
[36m@@ -204,7 +204,14 @@[m [mpublic class Game {[m
     }[m
 [m
     public List<Player> winners() {[m
[31m-        throw new UnsupportedOperationException("Not implemented");[m
[32m+[m[32m    int highestScore = players.stream()[m
[32m+[m[32m            .mapToInt(Player::getScore)[m
[32m+[m[32m            .max()[m
[32m+[m[32m            .orElseThrow();[m
[32m+[m
[32m+[m[32m    return players.stream()[m
[32m+[m[32m            .filter(player -> player.getScore() == highestScore)[m
[32m+[m[32m            .toList();[m
     }[m
 [m
     public Board getBoard() {[m
[1mdiff --git a/backend/src/test/java/com/carcassonne/domain/game/GameWinnersTest.java b/backend/src/test/java/com/carcassonne/domain/game/GameWinnersTest.java[m
[1mindex 21818e6..a1ac6d0 100644[m
[1m--- a/backend/src/test/java/com/carcassonne/domain/game/GameWinnersTest.java[m
[1m+++ b/backend/src/test/java/com/carcassonne/domain/game/GameWinnersTest.java[m
[36m@@ -1,2 +1,53 @@[m
[31m-package com.carcassonne.domain.game;public class GameWinnersTest {[m
[31m-}[m
[32m+[m[32mpackage com.carcassonne.domain.game;[m
[32m+[m
[32m+[m[32mimport com.carcassonne.domain.player.Player;[m
[32m+[m[32mimport com.carcassonne.domain.player.PlayerColor;[m
[32m+[m[32mimport org.junit.jupiter.api.Test;[m
[32m+[m
[32m+[m[32mimport java.util.List;[m
[32m+[m
[32m+[m[32mimport static org.junit.jupiter.api.Assertions.assertEquals;[m
[32m+[m
[32m+[m[32mclass GameWinnersTest {[m
[32m+[m
[32m+[m[32m    @Test[m
[32m+[m[32m    void returnsSinglePlayerWithHighestScore() {[m
[32m+[m[32m        Player alice = new Player("Alice", PlayerColor.RED, 30);[m
[32m+[m[32m        Player bob = new Player("Bob", PlayerColor.BLUE, 20);[m
[32m+[m
[32m+[m[32m        Game game = new Game(List.of(alice, bob));[m
[32m+[m[32m        game.finish();[m
[32m+[m
[32m+[m[32m        List<Player> winners = game.winners();[m
[32m+[m
[32m+[m[32m        assertEquals(List.of(alice), winners);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @Test[m
[32m+[m[32m    void returnsAllPlayersTiedForHighestScore() {[m
[32m+[m[32m        Player alice = new Player("Alice", PlayerColor.RED, 30);[m
[32m+[m[32m        Player bob = new Player("Bob", PlayerColor.BLUE, 30);[m
[32m+[m[32m        Player carol = new Player("Carol", PlayerColor.GREEN, 15);[m
[32m+[m
[32m+[m[32m        Game game = new Game(List.of(alice, bob, carol));[m
[32m+[m[32m        game.finish();[m
[32m+[m
[32m+[m[32m        List<Player> winners = game.winners();[m
[32m+[m
[32m+[m[32m        assertEquals(List.of(alice, bob), winners);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @Test[m
[32m+[m[32m    void returnsAllPlayersWhenEveryScoreIsEqual() {[m
[32m+[m[32m        Player alice = new Player("Alice", PlayerColor.RED);[m
[32m+[m[32m        Player bob = new Player("Bob", PlayerColor.BLUE);[m
[32m+[m[32m        Player carol = new Player("Carol", PlayerColor.GREEN);[m
[32m+[m
[32m+[m[32m        Game game = new Game(List.of(alice, bob, carol));[m
[32m+[m[32m        game.finish();[m
[32m+[m
[32m+[m[32m        List<Player> winners = game.winners();[m
[32m+[m
[32m+[m[32m        assertEquals(List.of(alice, bob, carol), winners);[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
\ No newline at end of file[m
