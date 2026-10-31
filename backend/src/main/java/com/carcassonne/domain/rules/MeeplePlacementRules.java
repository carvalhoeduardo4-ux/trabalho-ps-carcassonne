package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.player.MeeplePlacement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Pure Fabrication. */
@Component
public class MeeplePlacementRules {

    private final FeatureConnectivity connectivity;

    public MeeplePlacementRules() {
        this(null);
    }

    public MeeplePlacementRules(FeatureConnectivity connectivity) {
        this.connectivity = connectivity;
    }

    public boolean canPlace(Board board, List<MeeplePlacement> meeples, FeatureRef feature) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<FeatureRef> legalFeatures(Board board, List<MeeplePlacement> meeples, PlacedTile placedTile) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
