package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Rotation;
import java.util.Objects;

public class Placement {
    private final Position position;
    private final Rotation rotation;

    public Placement(Position position, Rotation rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placement placement = (Placement) o;
        return Objects.equals(position, placement.position) && rotation == placement.rotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, rotation);
    }

    @Override
    public String toString() {
        return "Placement(" + position + ", " + rotation + ")";
    }
}
