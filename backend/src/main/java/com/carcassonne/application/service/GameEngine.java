package com.carcassonne.application.service;

import com.carcassonne.application.port.in.GameInputPort;
import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.exception.InvalidMeeplePlacementException;
import com.carcassonne.domain.exception.InvalidPlacementException;
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

    public GameEngine() {
        this(null, null, null);
    }

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
        this.game = new Game(domainPlayers);
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
                Tile tile = g.drawTile();
                if (tileRules.legalPlacements(g.getBoard(), tile).isEmpty()) {
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
        Game g = requireGame();
        g.getTurn().requirePlaceTile();
        Tile tile = g.currentDrawnTile().orElseThrow();
        return tileRules.legalPlacements(g.getBoard(), tile);
    }

    @Override
    public void placeTile(Placement placement) {
        Game g = requireGame();
        g.getTurn().requirePlaceTile();
        Tile tile = g.currentDrawnTile().orElseThrow();
        if (!tileRules.canPlace(g.getBoard(), tile, placement)) {
            throw new InvalidPlacementException("Colocação de peça inválida: " + placement);
        }
        g.placeTile(placement);
    }

    @Override
    public List<FeatureRef> getLegalMeepleFeatures() {
        Game g = requireGame();
        g.getTurn().requireMeepleDecision();
        if (g.availableMeeples(g.currentPlayer()) <= 0) {
            return List.of();
        }
        return meepleRules.legalFeatures(g.getBoard(), g.getMeeples(), g.currentPlacedTile().orElseThrow());
    }

    @Override
    public void placeMeeple(FeatureRef feature) {
        Game g = requireGame();
        g.getTurn().requireMeepleDecision();
        if (!meepleRules.canPlace(g.getBoard(), g.getMeeples(), feature)) {
            throw new InvalidMeeplePlacementException("Colocação de meeple inválida: " + feature);
        }
        g.placeMeeple(feature);
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

