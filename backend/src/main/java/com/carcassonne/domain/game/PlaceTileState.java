package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.tile.Tile;

/** Fase PLACE_TILE do turno. */
public class PlaceTileState implements TurnState {

    private static InvalidTurnPhaseException invalid(String action) {
        return new InvalidTurnPhaseException("Ação '" + action + "' inválida na fase PLACE_TILE.");
    }
    @Override
    public void requireDrawTile() {
        throw invalid("drawTile");
    }

    @Override
    public void requirePlaceTile() {
        
    }

    @Override
    public void requireMeepleDecision() {
        throw invalid("meepleDecision");
    }

    @Override
    public void discardDrawnTile(Turn turn) {
        turn.reset();
    }

    @Override
    public void recordDrawnTile(Turn turn, Tile tile) {
        throw invalid("drawTile");
    }

    @Override
    public void recordPlacedTile(Turn turn, PlacedTile tile) {
        turn.setPlacedTile(tile);
        turn.setState(new PlaceMeepleState());
    }

    @Override
    public void resolveMeeple(Turn turn) {
        throw invalid("meepleDecision");
    }
}

