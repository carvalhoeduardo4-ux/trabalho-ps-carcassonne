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
        return features.stream()
                .filter(f -> f.getId() == featureId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Feature inexistente: " + featureId));
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane) {
        return featureAt(side, lane, Rotation.DEG_0);
    }

    /** Feature que ocupa a borda indicada (lane CENTER, ou LEFT/RIGHT se não houver). */
    public Optional<TileFeature> edgeFeature(Direction side, Rotation rotation) {
        Optional<TileFeature> center = featureAt(side, EdgeLane.CENTER, rotation);
        if (center.isPresent()) {
            return center;
        }
        Optional<TileFeature> left = featureAt(side, EdgeLane.LEFT, rotation);
        return left.isPresent() ? left : featureAt(side, EdgeLane.RIGHT, rotation);
    }

    /** Feature no lado/faixa observados após girar a peça no sentido horário. */
    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane, Rotation rotation) {
        Direction localSide = side.rotateBack(rotation);
        return features.stream()
                .filter(f -> f.getEdgeContacts().stream()
                        .anyMatch(c -> c.getSide() == localSide && c.getLane() == lane))
                .findFirst();
    }
}
