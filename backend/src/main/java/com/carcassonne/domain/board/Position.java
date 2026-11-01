package com.carcassonne.domain.board;

import java.util.Objects;

public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /** Posição vizinha na direção dada (NORTH = y + 1). */
    public Position neighbor(com.carcassonne.domain.tile.Direction direction) {
        return switch (direction) {
            case NORTH -> new Position(x, y + 1);
            case SOUTH -> new Position(x, y - 1);
            case EAST -> new Position(x + 1, y);
            case WEST -> new Position(x - 1, y);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

