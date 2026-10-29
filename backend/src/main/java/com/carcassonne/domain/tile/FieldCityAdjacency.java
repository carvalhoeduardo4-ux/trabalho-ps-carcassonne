package com.carcassonne.domain.tile;

import lombok.Value;

@Value
public class FieldCityAdjacency {
    TileFeature field;
    TileFeature city;
}
