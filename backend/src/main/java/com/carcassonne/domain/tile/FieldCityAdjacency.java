package com.carcassonne.domain.tile;

import java.util.Objects;

public class FieldCityAdjacency {
    private final TileFeature field;
    private final TileFeature city;

    public FieldCityAdjacency(TileFeature field, TileFeature city) {
        this.field = field;
        this.city = city;
    }

    public TileFeature getField() {
        return field;
    }

    public TileFeature getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldCityAdjacency that = (FieldCityAdjacency) o;
        return Objects.equals(field, that.field) && Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, city);
    }
}
