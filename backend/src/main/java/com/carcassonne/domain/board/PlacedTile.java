package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;

import java.util.Objects;
import java.util.Optional;

/** Imutável. */
public class PlacedTile {

    private final Tile tile;
    private final Position position;
    private final Rotation rotation;

    public PlacedTile(Tile tile, Position position, Rotation rotation) {
        this.tile = tile;
        this.position = position;
        this.rotation = rotation;
    }

    public Tile getTile() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public Optional<TileFeature> edgeFeature(Direction side) {
        return tile.edgeFeature(side, rotation);
    }

    public TileFeature feature(int featureId) {
        return tile.feature(featureId);
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane) {
        return tile.featureAt(side, lane, rotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlacedTile that = (PlacedTile) o;
        return Objects.equals(tile, that.tile) && Objects.equals(position, that.position) && rotation == that.rotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tile, position, rotation);
    }

    @Override
    public String toString() {
        return "PlacedTile(pos=" + position + ", rot=" + rotation + ")";
    }
}

