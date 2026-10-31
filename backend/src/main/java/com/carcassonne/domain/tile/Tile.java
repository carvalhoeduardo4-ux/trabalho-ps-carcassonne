package com.carcassonne.domain.tile;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/** Imutável / GRASP Information Expert. */
public class Tile {

    private final List<TileFeature> features;                       // 1..*
    private final List<FieldCityAdjacency> fieldCityAdjacencies;    // 0..*

    public Tile(List<TileFeature> features, List<FieldCityAdjacency> fieldCityAdjacencies) {
        this.features = features != null ? features : List.of();
        this.fieldCityAdjacencies = fieldCityAdjacencies != null ? fieldCityAdjacencies : List.of();
    }

    public Tile(List<TileFeature> features) {
        this(features, List.of());
    }

    public List<TileFeature> getFeatures() {
        return features;
    }

    public List<FieldCityAdjacency> getFieldCityAdjacencies() {
        return fieldCityAdjacencies;
    }


    public TileFeature feature(int featureId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
