package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

@Component
public class MonasteryScoringStrategy implements FeatureScoringStrategy {

    @Override
    public int completedScore(Board board, FeatureRegion region) {
        return 9;
    }

    /** 1 ponto pela peça do mosteiro mais 1 por peça ao redor. */
    @Override
    public int endGameScore(Board board, FeatureRegion region) {
        return 1 + board.surrounding(region.getMembers().get(0).getPosition()).size();
    }
}
