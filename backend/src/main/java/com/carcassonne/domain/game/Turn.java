package com.carcassonne.domain.game;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
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
    private boolean meepleResolved;
    private boolean completedFeaturesScored;

    /** Snapshot independente, compartilhando somente peças imutáveis e estados sem dados. */
    public Turn copy() {
        Turn copy = new Turn();
        copy.state = state;
        copy.drawnTile = drawnTile;
        copy.placedTile = placedTile;
        copy.meepleResolved = meepleResolved;
        copy.completedFeaturesScored = completedFeaturesScored;
        return copy;
    }

    void requireCompletedScoring() {
        state.requireMeepleDecision();
        if (!meepleResolved || completedFeaturesScored) {
            throw new InvalidTurnPhaseException(
                    "Pontuação exige decisão de meeple resolvida e ainda não pontuada.");
        }
    }

    void recordCompletedScoring() {
        requireCompletedScoring();
        completedFeaturesScored = true;
    }

    void requireCompletedScored() {
        if (!completedFeaturesScored) {
            throw new InvalidTurnPhaseException("A pontuação deste turno ainda não foi resolvida.");
        }
    }

    public void requireDrawTile() {
        state.requireDrawTile();
    }

    public void requirePlaceTile() {
        state.requirePlaceTile();
    }

    public void requireMeepleDecision() {
        state.requireMeepleDecision();
        if (meepleResolved) {
            throw new com.carcassonne.domain.exception.InvalidTurnPhaseException(
                    "A decisão de meeple já foi tomada neste turno.");
        }
    }

    void markMeepleResolved() {
        this.meepleResolved = true;
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
        this.meepleResolved = false;
        this.completedFeaturesScored = false;
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
