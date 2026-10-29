package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.game.Game;
import org.springframework.stereotype.Component;

import java.util.List;

/** Pure Fabrication. */
@Component
public class TilePlacementRules {

    public boolean canPlace(Game game, Placement placement) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Placement> legalPlacements(Game game) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
