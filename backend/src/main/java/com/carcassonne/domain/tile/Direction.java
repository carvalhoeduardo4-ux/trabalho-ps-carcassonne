package com.carcassonne.domain.tile;

public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public Direction opposite() {
        return rotate(Rotation.DEG_180);
    }

    /** Gira a direção no sentido horário. */
    public Direction rotate(Rotation rotation) {
        return values()[(ordinal() + rotation.quarterTurns()) % 4];
    }

    /** Gira a direção no sentido anti-horário (inverso de {@link #rotate}). */
    public Direction rotateBack(Rotation rotation) {
        return values()[(ordinal() - rotation.quarterTurns() + 4) % 4];
    }
}
