package com.carcassonne.domain.player;

import com.carcassonne.domain.board.FeatureRef;
import lombok.Value;

@Value
public class MeeplePlacement {
    Player player;
    FeatureRef feature;
}
