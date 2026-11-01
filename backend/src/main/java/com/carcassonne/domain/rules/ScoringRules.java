package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.tile.FeatureType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<ScoreAward> awards = new ArrayList<>();
        Set<Set<FeatureRef>> seen = new HashSet<>();

        List<FeatureRef> candidates = new ArrayList<>();
        placedTile.getTile().getFeatures().forEach(f ->
                candidates.add(new FeatureRef(placedTile.getPosition(), f.getId())));
        // Mosteiros vizinhos podem ter sido completados por esta peça.
        for (PlacedTile around : board.surrounding(placedTile.getPosition())) {
            around.getTile().getFeatures().stream()
                    .filter(f -> f.getType() == FeatureType.MONASTERY)
                    .forEach(f -> candidates.add(new FeatureRef(around.getPosition(), f.getId())));
        }

        for (FeatureRef candidate : candidates) {
            FeatureType type = placedTile.getPosition().equals(candidate.getPosition())
                    ? placedTile.feature(candidate.getFeatureId()).getType()
                    : board.tileAt(candidate.getPosition()).orElseThrow().feature(candidate.getFeatureId()).getType();
            if (type == FeatureType.FIELD) {
                continue;
            }
            FeatureRegion region = connectivity.connectedRegion(board, candidate);
            if (!seen.add(new HashSet<>(region.getMembers())) || !connectivity.isComplete(board, region)) {
                continue;
            }
            scoreRegion(board, meeples, region).ifPresent(awards::add);
        }
        return awards;
    }

    private java.util.Optional<ScoreAward> scoreRegion(Board board, List<MeeplePlacement> meeples, FeatureRegion region) {
        Map<Player, Integer> counts = new LinkedHashMap<>();
        for (MeeplePlacement m : meeples) {
            if (region.contains(m.getFeature())) {
                counts.merge(m.getPlayer(), 1, Integer::sum);
            }
        }
        if (counts.isEmpty()) {
            return java.util.Optional.empty();
        }
        int max = counts.values().stream().max(Integer::compare).orElse(0);
        List<Player> winners = counts.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();
        int points = strategyFor(region.getType()).completedScore(board, region);
        return java.util.Optional.of(new ScoreAward(points, winners, region));
    }

    public List<ScoreAward> endGameAwards(Board board, List<MeeplePlacement> meeples) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private FeatureScoringStrategy strategyFor(FeatureType type) {
        return strategiesByType.get(type);
    }
}
