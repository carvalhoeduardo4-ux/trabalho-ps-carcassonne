package com.carcassonne.domain.board;

import java.util.Objects;

public class FeatureRef {
    private final Position position;
    private final int featureId;

    public FeatureRef(Position position, int featureId) {
        this.position = position;
        this.featureId = featureId;
    }

    public Position getPosition() {
        return position;
    }

    public int getFeatureId() {
        return featureId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureRef that = (FeatureRef) o;
        return featureId == that.featureId && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, featureId);
    }

    @Override
    public String toString() {
        return "FeatureRef(" + position + ", #" + featureId + ")";
    }
}
