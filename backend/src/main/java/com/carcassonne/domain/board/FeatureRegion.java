package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.FeatureType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FeatureRegion {
    private final FeatureType type;
    private final List<FeatureRef> members; // 1..*

    public FeatureRegion(FeatureType type, List<FeatureRef> members) {
        this.type = type;
        this.members = members != null ? Collections.unmodifiableList(members) : Collections.emptyList();
    }

    public FeatureType getType() {
        return type;
    }

    public List<FeatureRef> getMembers() {
        return members;
    }

    public boolean contains(FeatureRef feature) {
        return members.contains(feature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureRegion that = (FeatureRegion) o;
        return type == that.type && Objects.equals(members, that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, members);
    }

    @Override
    public String toString() {
        return "FeatureRegion(" + type + ", members=" + members.size() + ")";
    }
}
