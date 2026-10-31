package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.tile.Tile;
import org.springframework.stereotype.Component;

import java.util.List;

/** Pure Fabrication. */
@Component
public class TilePlacementRules {

    public boolean canPlace(Board board, Tile tile, Placement placement) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Placement> legalPlacements(Board board, Tile tile) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
