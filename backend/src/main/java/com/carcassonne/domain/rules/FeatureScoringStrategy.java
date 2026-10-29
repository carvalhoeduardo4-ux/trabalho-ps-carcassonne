package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRegion;

/** Strategy. */
public interface FeatureScoringStrategy {

    int completedScore(Board board, FeatureRegion region);

    int endGameScore(Board board, FeatureRegion region);
}
