package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.tile.Tile;

/** Fase PLACE_MEEPLE do turno. */
public class PlaceMeepleState implements TurnState {

    private static InvalidTurnPhaseException invalid(String action) {
        return new InvalidTurnPhaseException("Ação '" + action + "' inválida na fase PLACE_MEEPLE.");
    }
    @Override
    public void requireDrawTile() {
        throw invalid("drawTile");
    }

    @Override
    public void requirePlaceTile() {
        throw invalid("placeTile");
    }

    @Override
    public void requireMeepleDecision() {
        
    }

    @Override
    public void discardDrawnTile(Turn turn) {
        throw invalid("discardDrawnTile");
    }

    @Override
    public void recordDrawnTile(Turn turn, Tile tile) {
        throw invalid("drawTile");
    }

    @Override
    public void recordPlacedTile(Turn turn, PlacedTile tile) {
        throw invalid("placeTile");
    }

    @Override
    public void resolveMeeple(Turn turn) {
        turn.markMeepleResolved();
    }
}

