package com.carcassonne.domain.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.carcassonne.domain.tile.Direction;

import java.util.Collections;

/** GRASP Information Expert. */
public class Board {

    private final List<PlacedTile> tiles = new ArrayList<>();

    public boolean isOccupied(Position position) {
        return tileAt(position).isPresent();
    }

    public Optional<PlacedTile> tileAt(Position position) {
        if (position == null) {
            return Optional.empty();
        }
        return tiles.stream()
                .filter(t -> t.getPosition().equals(position))
                .findFirst();
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
        if (tile == null) {
            throw new IllegalArgumentException("Peça posicionada não pode ser nula");
        }
        this.tiles.add(tile);
    }

    public List<PlacedTile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }
}
