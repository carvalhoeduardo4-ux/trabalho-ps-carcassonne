package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoadScoringStrategy implements FeatureScoringStrategy {

    /** 1 ponto por peça da estrada. */
    @Override
    public int completedScore(Board board, FeatureRegion region) {
        return tileCount(region);
    }

    @Override
    public int endGameScore(Board board, FeatureRegion region) {
        return tileCount(region);
    }

    private int tileCount(FeatureRegion region) {
        return (int) region.getMembers().stream().map(FeatureRef::getPosition).distinct().count();
    }
}
