package com.carcassonne.domain.player;

import java.util.Objects;

/** GRASP Information Expert. */
public class Player {

    private final String name;
    private final PlayerColor color;
    private int score;

    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
        this.score = 0;
    }

    public Player(String name, PlayerColor color, int score) {
        this.name = name;
        this.color = color;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) && color == player.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return "Player(" + name + ", " + color + ", score=" + score + ")";
    }
}
