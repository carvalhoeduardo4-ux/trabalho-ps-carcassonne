package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.tile.Tile;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.Optional;

/** State pattern - Context. */
public class Turn {

    @Setter(AccessLevel.PACKAGE)
    private TurnState state = new DrawTileState();
    @Setter(AccessLevel.PACKAGE)
    private Tile drawnTile;          // 0..1
    @Setter(AccessLevel.PACKAGE)
    private PlacedTile placedTile;   // 0..1

    public void requireDrawTile() {
        state.requireDrawTile();
    }

    public void requirePlaceTile() {
        state.requirePlaceTile();
    }

    public void requireMeepleDecision() {
        state.requireMeepleDecision();
    }

    public void discardDrawnTile() {
        state.discardDrawnTile(this);
    }

    public void recordDrawnTile(Tile tile) {
        state.recordDrawnTile(this, tile);
    }

    public void recordPlacedTile(PlacedTile tile) {
        state.recordPlacedTile(this, tile);
    }

    public void resolveMeeple() {
        state.resolveMeeple(this);
    }

    public void reset() {
        this.state = new DrawTileState();
        this.drawnTile = null;
        this.placedTile = null;
    }

    public TurnState getState() {
        return state;
    }


    public Optional<Tile> drawnTile() {
        return Optional.ofNullable(drawnTile);
    }

    public Optional<PlacedTile> placedTile() {
        return Optional.ofNullable(placedTile);
    }
}
