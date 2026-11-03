package com.carcassonne.domain.player;

import java.util.Objects;

/** GRASP Information Expert. */
public class Player {

    private final String name;
    private final PlayerColor color;
    private final PlayerType type;
    private int score;

    public Player(String name, PlayerColor color) {
        this(name, color, 0, PlayerType.HUMAN);
    }

    public Player(String name, PlayerColor color, int score) {
        this(name, color, score, PlayerType.HUMAN);
    }

    public Player(String name, PlayerColor color, PlayerType type) {
        this(name, color, 0, type);
    }

    public Player(String name, PlayerColor color, int score, PlayerType type) {
        this.name = name;
        this.color = color;
        this.score = score;
        this.type = type != null ? type : PlayerType.HUMAN;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public PlayerType getType() {
        return type;
    }

    public boolean isAi() {
        return type == PlayerType.AI;
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
        return "Player(" + name + ", " + color + ", " + type + ", score=" + score + ")";
    }
}
