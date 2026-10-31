package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.tile.FeatureType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Pure Fabrication. */
@Component
public class ScoringRules {

    private final FeatureConnectivity connectivity;
    private final Map<FeatureType, FeatureScoringStrategy> strategiesByType; // 1..*

    public ScoringRules() {
        this(null, Map.of());
    }

    public ScoringRules(FeatureConnectivity connectivity, Map<FeatureType, FeatureScoringStrategy> strategiesByType) {
        this.connectivity = connectivity;
        this.strategiesByType = strategiesByType != null ? strategiesByType : Map.of();
    }

    @Autowired
    public ScoringRules(FeatureConnectivity connectivity,
                        RoadScoringStrategy road,
                        CityScoringStrategy city,
                        MonasteryScoringStrategy monastery,
                        FieldScoringStrategy field) {
        this(connectivity, Map.of(
                FeatureType.ROAD, road,
                FeatureType.CITY, city,
                FeatureType.MONASTERY, monastery,
                FeatureType.FIELD, field));
    }

    public List<ScoreAward> completedAwards(Board board, List<MeeplePlacement> meeples, PlacedTile placedTile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<ScoreAward> endGameAwards(Board board, List<MeeplePlacement> meeples) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private FeatureScoringStrategy strategyFor(FeatureType type) {
        return strategiesByType.get(type);
    }
}
