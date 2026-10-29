package com.carcassonne.domain.board;

import lombok.Value;

@Value
public class FeatureRef {
    Position position;
    int featureId;
}
