package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.game.ScoreAward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Pure Fabrication. */
@Component
public class ScoringRules {

    private final FeatureConnectivity connectivity;
    private final List<FeatureScoringStrategy> strategies;

    public ScoringRules() {
        this(null, List.of());
    }

    public ScoringRules(FeatureConnectivity connectivity, List<FeatureScoringStrategy> strategies) {
        this.connectivity = connectivity;
        this.strategies = strategies != null ? strategies : List.of();
    }

    public List<ScoreAward> completedAwards(Game game, PlacedTile placedTile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<ScoreAward> endGameAwards(Game game) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
