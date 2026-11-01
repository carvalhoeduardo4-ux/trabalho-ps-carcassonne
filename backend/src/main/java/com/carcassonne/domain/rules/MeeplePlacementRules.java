package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.player.MeeplePlacement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Pure Fabrication. */
@Component
public class MeeplePlacementRules {

    private final FeatureConnectivity connectivity;

    public MeeplePlacementRules() {
        this(new FeatureConnectivity());
    }

    @Autowired
    public MeeplePlacementRules(FeatureConnectivity connectivity) {
        this.connectivity = connectivity != null ? connectivity : new FeatureConnectivity();
    }

    /** A feature existe e nenhuma peça da região conectada possui meeple (RN06). */
    public boolean canPlace(Board board, List<MeeplePlacement> meeples, FeatureRef feature) {
        if (board == null || feature == null) {
            return false;
        }
        Optional<PlacedTile> tile = board.tileAt(feature.getPosition());
        if (tile.isEmpty() || tile.get().getTile().getFeatures().stream()
                .noneMatch(f -> f.getId() == feature.getFeatureId())) {
            return false;
        }
        FeatureRegion region = connectivity.connectedRegion(board, feature);
        return meeples == null || meeples.stream().noneMatch(m -> region.contains(m.getFeature()));
    }

    public List<FeatureRef> legalFeatures(Board board, List<MeeplePlacement> meeples, PlacedTile placedTile) {
        List<FeatureRef> result = new ArrayList<>();
        if (placedTile == null) {
            return result;
        }
        placedTile.getTile().getFeatures().forEach(f -> {
            FeatureRef ref = new FeatureRef(placedTile.getPosition(), f.getId());
            if (canPlace(board, meeples, ref)) {
                result.add(ref);
            }
        });
        return result;
    }
}
