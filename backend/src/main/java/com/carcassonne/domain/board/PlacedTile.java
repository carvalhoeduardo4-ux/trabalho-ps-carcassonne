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
    private final Placement placement;

    public PlacedTile(Tile tile, Placement placement) {
        this.tile = Objects.requireNonNull(tile, "Tile não pode ser nulo.");
        this.placement = Objects.requireNonNull(placement, "Placement não pode ser nulo.");
    }

    public Tile getTile() {
        return tile;
    }

    public Placement getPlacement() {
        return placement;
    }

    public Position getPosition() {
        return placement.getPosition();
    }

    public Rotation getRotation() {
        return placement.getRotation();
    }

    public Optional<TileFeature> edgeFeature(Direction side) {
        Optional<TileFeature> center = featureAt(side, EdgeLane.CENTER);
        if (center.isPresent()) {
            return center;
        }
        Optional<TileFeature> left = featureAt(side, EdgeLane.LEFT);
        return left.isPresent() ? left : featureAt(side, EdgeLane.RIGHT);
    }

    public TileFeature feature(int featureId) {
        return tile.feature(featureId);
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane) {
        Direction localSide = side.rotateBack(placement.getRotation());
        return tile.featureAt(localSide, lane);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlacedTile that = (PlacedTile) o;
        return Objects.equals(tile, that.tile) && Objects.equals(placement, that.placement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tile, placement);
    }

    @Override
    public String toString() {
        return "PlacedTile(pos=" + getPosition() + ", rot=" + getRotation() + ")";
    }
}
