package com.carcassonne.domain.tile;

import java.util.ArrayList;
import java.util.List;

/** GRASP Information Expert. */
public class TileDeck {

    private final List<Tile> remaining = new ArrayList<>();

    public void shuffle() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Tile draw() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int remaining() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
