package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.tile.Tile;

public class DrawTileState implements TurnState {

    @Override
    public void recordDrawnTile(Turn turn, Tile tile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void recordPlacedTile(Turn turn, PlacedTile tile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void resolveMeeple(Turn turn) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
