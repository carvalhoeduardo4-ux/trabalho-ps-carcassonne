package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.FeatureType;
import lombok.Value;

import java.util.List;

@Value
public class FeatureRegion {
    FeatureType type;
    List<FeatureRef> members; // 1..*

    public boolean contains(FeatureRef feature) {
        return members.contains(feature);
    }
}
