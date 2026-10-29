package com.carcassonne.application.service;

import com.carcassonne.application.port.in.GameInputPort;
import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Tile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** GRASP Controller. */
@Service
@RequiredArgsConstructor
public class GameEngine implements GameInputPort {

    private Game game; // 0..1
    private final TilePlacementRules tileRules;
    private final MeeplePlacementRules meepleRules;
    private final ScoringRules scoringRules;

    @Override
    public void startGame(List<PlayerSetup> players) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Tile> drawTile() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Placement> getLegalPlacements() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void placeTile(Placement placement) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<FeatureRef> getLegalMeepleFeatures() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void placeMeeple(FeatureRef feature) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void skipMeeple() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void resolveTurn() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void finishGame() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
