package com.carcassonne.domain.tile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Imutável. */
public class TileFeature {
    private final int id;
    private final FeatureType type;
    private final int shieldCount;
    private final List<EdgeContact> edgeContacts; // 0..*

    public TileFeature(int id, FeatureType type, int shieldCount, List<EdgeContact> edgeContacts) {
        this.id = id;
        this.type = type;
        this.shieldCount = shieldCount;
        this.edgeContacts = edgeContacts != null ? Collections.unmodifiableList(edgeContacts) : Collections.emptyList();
    }

    public int getId() {
        return id;
    }

    public FeatureType getType() {
        return type;
    }

    public int getShieldCount() {
        return shieldCount;
    }

    public List<EdgeContact> getEdgeContacts() {
        return edgeContacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileFeature that = (TileFeature) o;
        return id == that.id && shieldCount == that.shieldCount && type == that.type && Objects.equals(edgeContacts, that.edgeContacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, shieldCount, edgeContacts);
    }

    @Override
    public String toString() {
        return "TileFeature(id=" + id + ", type=" + type + ", shield=" + shieldCount + ")";
    }
}
