package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Pure Fabrication. */
@Component
public class TilePlacementRules {

    public boolean canPlace(Board board, Tile tile, Placement placement) {
        if (board == null || tile == null || placement == null
                || placement.getPosition() == null || placement.getRotation() == null) {
            return false;
        }
        Position position = placement.getPosition();
        Rotation rotation = placement.getRotation();

        if (board.isOccupied(position)) {
            return false;
        }
        Map<Direction, PlacedTile> neighbors = board.neighbors(position);
        if (neighbors.isEmpty()) {
            return false;
        }
        for (Map.Entry<Direction, PlacedTile> entry : neighbors.entrySet()) {
            Direction side = entry.getKey();
            Optional<TileFeature> mine = tile.edgeFeature(side, rotation);
            Optional<TileFeature> theirs = entry.getValue().edgeFeature(side.opposite());
            if (mine.isEmpty() || theirs.isEmpty() || mine.get().getType() != theirs.get().getType()) {
                return false;
            }
        }
        return true;
    }

    public List<Placement> legalPlacements(Board board, Tile tile) {
        List<Placement> result = new ArrayList<>();
        if (board == null || tile == null) {
            return result;
        }
        for (Position position : board.frontierPositions()) {
            for (Rotation rotation : Rotation.values()) {
                Placement candidate = new Placement(position, rotation);
                if (canPlace(board, tile, candidate)) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }
}
