package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import lombok.Value;

import java.util.Optional;

/** Imutável. */
@Value
public class PlacedTile {

    Tile tile;
    Position position;
    Rotation rotation;

    public TileFeature feature(int featureId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<TileFeature> featureAt(Direction side, EdgeLane lane) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
