package com.carcassonne.domain.ai;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;

import java.util.Objects;
import java.util.Optional;

/**
 * Value object: jogada escolhida pela IA.
 *
 * Somente o dado correspondente à ação é preenchido:
 * placement em PLACE_TILE, feature em PLACE_MEEPLE.
 */
public final class AiDecision {

    private final AiAction action;
    private final Placement placement;   // 0..1
    private final FeatureRef feature;    // 0..1

    private AiDecision(AiAction action, Placement placement, FeatureRef feature) {
        this.action = action;
        this.placement = placement;
        this.feature = feature;
    }

    public static AiDecision drawTile() {
        return new AiDecision(AiAction.DRAW_TILE, null, null);
    }

    public static AiDecision placeTile(Placement placement) {
        return new AiDecision(
                AiAction.PLACE_TILE,
                Objects.requireNonNull(placement, "Placement não pode ser nulo."),
                null
        );
    }

    public static AiDecision placeMeeple(FeatureRef feature) {
        return new AiDecision(
                AiAction.PLACE_MEEPLE,
                null,
                Objects.requireNonNull(feature, "FeatureRef não pode ser nulo.")
        );
    }

    public static AiDecision skipMeeple() {
        return new AiDecision(AiAction.SKIP_MEEPLE, null, null);
    }

    public AiAction getAction() {
        return action;
    }

    public Optional<Placement> getPlacement() {
        return Optional.ofNullable(placement);
    }

    public Optional<FeatureRef> getFeature() {
        return Optional.ofNullable(feature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiDecision that = (AiDecision) o;
        return action == that.action
                && Objects.equals(placement, that.placement)
                && Objects.equals(feature, that.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, placement, feature);
    }

    @Override
    public String toString() {
        return switch (action) {
            case PLACE_TILE -> "AiDecision(PLACE_TILE, " + placement + ")";
            case PLACE_MEEPLE -> "AiDecision(PLACE_MEEPLE, " + feature + ")";
            default -> "AiDecision(" + action + ")";
        };
    }
}
