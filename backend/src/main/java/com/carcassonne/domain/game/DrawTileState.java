package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.tile.Tile;

/** Fase DRAW_TILE do turno. */
public class DrawTileState implements TurnState {

    private static InvalidTurnPhaseException invalid(String action) {
        return new InvalidTurnPhaseException("Ação '" + action + "' inválida na fase DRAW_TILE.");
    }
    @Override
    public void requireDrawTile() {
        
    }

    @Override
    public void requirePlaceTile() {
        throw invalid("placeTile");
    }

    @Override
    public void requireMeepleDecision() {
        throw invalid("meepleDecision");
    }

    @Override
    public void discardDrawnTile(Turn turn) {
        throw invalid("discardDrawnTile");
    }

    @Override
    public void recordDrawnTile(Turn turn, Tile tile) {
        turn.setDrawnTile(tile);
        turn.setState(new PlaceTileState());
    }

    @Override
    public void recordPlacedTile(Turn turn, PlacedTile tile) {
        throw invalid("placeTile");
    }

    @Override
    public void resolveMeeple(Turn turn) {
        throw invalid("meepleDecision");
    }
}

