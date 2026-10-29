package com.carcassonne.domain.tile;

import lombok.Value;

import java.util.List;

/** Imutável. */
@Value
public class TileFeature {
    int id;
    FeatureType type;
    int shieldCount;
    List<EdgeContact> edgeContacts; // 0..*
}
