package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

@Component
public class CityScoringStrategy implements FeatureScoringStrategy {

    /** Cidade completa: 2 pontos por peça e 2 por escudo. */
    @Override
    public int completedScore(Board board, FeatureRegion region) {
        return 2 * (tileCount(region) + shields(board, region));
    }

    /** Cidade incompleta: 1 ponto por peça e 1 por escudo. */
    @Override
    public int endGameScore(Board board, FeatureRegion region) {
        return tileCount(region) + shields(board, region);
    }

    private int tileCount(FeatureRegion region) {
        return (int) region.getMembers().stream().map(FeatureRef::getPosition).distinct().count();
    }

    private int shields(Board board, FeatureRegion region) {
        return region.getMembers().stream()
                .mapToInt(ref -> board.tileAt(ref.getPosition()).orElseThrow()
                        .feature(ref.getFeatureId()).getShieldCount())
                .sum();
    }
}
