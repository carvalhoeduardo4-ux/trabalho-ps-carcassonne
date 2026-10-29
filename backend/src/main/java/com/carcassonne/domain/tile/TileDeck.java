package com.carcassonne.domain.tile;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import java.util.NoSuchElementException;

/** GRASP Information Expert. */
public class TileDeck {

    private final List<Tile> remaining = new ArrayList<>();

    public TileDeck() {
    }

    public TileDeck(List<Tile> tiles) {
        if (tiles != null) {
            this.remaining.addAll(tiles);
        }
    }

    public void shuffle() {
        Collections.shuffle(remaining);
    }

    public Tile draw() {
        if (remaining.isEmpty()) {
            throw new NoSuchElementException("Monte de peças está vazio");
        }
        return remaining.remove(0);
    }

    public boolean isEmpty() {
        return remaining.isEmpty();
    }

    public int remaining() {
        return remaining.size();
    }

    public List<Tile> getRemainingTiles() {
        return Collections.unmodifiableList(remaining);
    }
}
