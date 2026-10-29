package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.tile.Tile;

/** State pattern - State. */
public interface TurnState {

    void recordDrawnTile(Turn turn, Tile tile);

    void recordPlacedTile(Turn turn, PlacedTile tile);

    void resolveMeeple(Turn turn);
}
