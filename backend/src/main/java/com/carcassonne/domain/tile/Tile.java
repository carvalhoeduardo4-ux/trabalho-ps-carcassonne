package com.carcassonne.domain.tile;

import java.util.List;
import java.util.Optional;

/** Imutável / GRASP Information Expert. */
public class Tile {

    private final String id;                                         // tipo da peça (A..X, START)
    private final List<TileFeature> features;                       // 1..*
    private final List<FieldCityAdjacency> fieldCityAdjacencies;    // 0..*

    public Tile(String id, List<TileFeature> features, List<FieldCityAdjacency> fieldCityAdjacencies) {
        this.id = id != null ? id : "UNKNOWN";
        this.features = features != null ? List.copyOf(features) : List.of();
        this.fieldCityAdjacencies = fieldCityAdjacencies != null ? List.copyOf(fieldCityAdjacencies) : List.of();
    }

    public Tile(String id, List<TileFeature> features) {
        this(id, features, List.of());
    }

    public Tile(List<TileFeature> features, List<FieldCityAdjacency> fieldCityAdjacencies) {
        this(null, features, fieldCityAdjacencies);
    }

    public Tile(List<TileFeature> features) {
        this(null, features, List.of());
    }

    public String getId() {
        return id;
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
        return features.stream()
                .filter(f -> f.getEdgeContacts().stream()
                        .anyMatch(c -> c.getSide() == side && c.getLane() == lane))
                .findFirst();
    }
}
