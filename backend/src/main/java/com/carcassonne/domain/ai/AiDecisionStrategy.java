package com.carcassonne.domain.ai;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;

import java.util.List;
import java.util.Optional;

public interface AiDecisionStrategy {

    Placement choosePlacement(List<Placement> legal);

    Optional<FeatureRef> chooseMeeple(List<FeatureRef> legal);
}
