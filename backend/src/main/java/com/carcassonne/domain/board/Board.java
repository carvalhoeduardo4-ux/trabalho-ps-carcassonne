package com.carcassonne.domain.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.carcassonne.domain.tile.Direction;

/** GRASP Information Expert. */
public class Board {

    private final List<PlacedTile> tiles = new ArrayList<>();

    public boolean isOccupied(Position position) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<PlacedTile> tileAt(Position position) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Map<Direction, PlacedTile> neighbors(Position position) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<PlacedTile> surrounding(Position position) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Set<Position> frontierPositions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void place(PlacedTile tile) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
