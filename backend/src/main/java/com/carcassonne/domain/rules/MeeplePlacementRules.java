package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.game.Game;
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

    public boolean canPlace(Game game, FeatureRef feature) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<FeatureRef> legalFeatures(Game game) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
