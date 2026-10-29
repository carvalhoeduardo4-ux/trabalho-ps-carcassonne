package com.carcassonne.domain.tile;

import java.util.Objects;

public class EdgeContact {
    private final Direction side;
    private final EdgeLane lane;

    public EdgeContact(Direction side, EdgeLane lane) {
        this.side = side;
        this.lane = lane;
    }

    public Direction getSide() {
        return side;
    }

    public EdgeLane getLane() {
        return lane;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeContact that = (EdgeContact) o;
        return side == that.side && lane == that.lane;
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, lane);
    }

    @Override
    public String toString() {
        return "EdgeContact(" + side + ", " + lane + ")";
    }
}
