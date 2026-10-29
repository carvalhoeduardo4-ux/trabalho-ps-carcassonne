package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

@Component
public class RoadScoringStrategy implements FeatureScoringStrategy {

    @Override
    public int completedScore(Board board, FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int endGameScore(Board board, FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
