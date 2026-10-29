package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRegion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FieldScoringStrategy implements FeatureScoringStrategy {

    private final FeatureConnectivity connectivity;

    @Override
    public int completedScore(Board board, FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int endGameScore(Board board, FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
