package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Pure Fabrication. */
@Component
public class FeatureConnectivity {

    public FeatureRegion connectedRegion(Board board, FeatureRef start) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isComplete(Board board, FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Set<FeatureRegion> adjacentCityRegions(Board board, FeatureRegion field) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
