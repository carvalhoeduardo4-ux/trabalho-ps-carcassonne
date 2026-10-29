package com.carcassonne.domain.tile;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/** Imutável / GRASP Information Expert. */
@RequiredArgsConstructor
public class Tile {

    private final List<TileFeature> features;                       // 1..*
    private final List<FieldCityAdjacency> fieldCityAdjacencies;    // 0..*

    public TileFeature feature(int featureId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane, Rotation rotation) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
