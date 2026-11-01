package com.carcassonne.application.service;

import com.carcassonne.application.port.in.GameInputPort;
import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.rules.ScoreAward;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Tile;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** GRASP Controller. */
@Service
public class GameEngine implements GameInputPort {

    @Getter
    private Game game; // 0..1
    private final TilePlacementRules tileRules;
    private final MeeplePlacementRules meepleRules;
    private final ScoringRules scoringRules;
    

    @Autowired
    public GameEngine(TilePlacementRules tileRules, MeeplePlacementRules meepleRules, ScoringRules scoringRules) {
        this.tileRules = tileRules;
        this.meepleRules = meepleRules;
        this.scoringRules = scoringRules;
    }

    @Override
    public void startGame(List<PlayerSetup> players) {
        if (players == null) {
            throw new IllegalArgumentException("A lista de configurações de jogadores não pode ser nula.");
        }
        List<Player> domainPlayers = new ArrayList<>();
        for (PlayerSetup setup : players) {
            if (setup == null) {
                throw new IllegalArgumentException("PlayerSetup não pode ser nulo.");
            }
            domainPlayers.add(new Player(setup.getName(), setup.getColor()));
        }
        this.game = new Game(
                domainPlayers,
                tileRules,
                meepleRules
        );
    }

    private Game requireGame() {
        if (game == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }
        return game;
    }

    @Override
    public Optional<Tile> drawTile() {
        Game g = requireGame();
        boolean replacementRequired = true;
        while (replacementRequired) {
            if (!g.hasTilesRemaining()) {
                replacementRequired = false;
            } else {
                g.drawTile();

                if (g.legalPlacements().isEmpty()) {
                    g.discardDrawnTile();
                } else {
                    replacementRequired = false;
                }
            }
        }
        Optional<Tile> drawn = g.currentDrawnTile();
        if (drawn.isEmpty()) {
            finishGame();
        }
        return drawn;
    }

    @Override
    public List<Placement> getLegalPlacements() {
        return requireGame().legalPlacements();
    }

    @Override
    public void placeTile(Placement placement) {
        requireGame().placeTile(placement);
    }

    @Override
    public List<FeatureRef> getLegalMeepleFeatures() {
        return requireGame().legalMeepleFeatures();
    }

    @Override
    public void placeMeeple(FeatureRef feature) {
        requireGame().placeMeeple(feature);
        resolveTurn();
    }

    @Override
    public void skipMeeple() {
        Game g = requireGame();
        g.skipMeeple();
        resolveTurn();
    }

    private void resolveTurn() {
        PlacedTile placed = game.currentPlacedTile().orElseThrow();
        List<ScoreAward> awards = scoringRules.completedAwards(game.getBoard(), game.getMeeples(), placed);
        game.applyAwards(awards);
        if (game.hasTilesRemaining()) {
            game.nextPlayer();
        } else {
            finishGame();
        }
    }

    private void finishGame() {
        game.finish();
    }
}

