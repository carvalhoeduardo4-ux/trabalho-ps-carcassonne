package com.carcassonne.application.service;

import com.carcassonne.application.port.in.AiInputPort;
import com.carcassonne.application.port.in.GameInputPort;
import com.carcassonne.domain.ai.AiDecision;
import com.carcassonne.domain.ai.AiDecisionException;
import com.carcassonne.domain.ai.AiDecisionStrategy;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.exception.NotAiPlayerTurnException;
import com.carcassonne.domain.game.DrawTileState;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.game.GameStatus;
import com.carcassonne.domain.game.PlaceMeepleState;
import com.carcassonne.domain.game.PlaceTileState;
import com.carcassonne.domain.game.TurnState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Pure Fabrication: decide a jogada dos jogadores controlados pelo computador.
 *
 * Apenas decide: consulta as opções legais pela GameInputPort, delega a
 * escolha para a AiDecisionStrategy e devolve a jogada. A execução
 * continua pelos mesmos métodos usados por jogadores humanos.
 */
@Service
public class AiPlayerService implements AiInputPort {

    private final GameInputPort game;
    private final AiDecisionStrategy strategy;

    public AiPlayerService(GameInputPort game, AiDecisionStrategy strategy) {
        this.game = Objects.requireNonNull(game, "GameInputPort não pode ser nulo.");
        this.strategy = Objects.requireNonNull(strategy, "AiDecisionStrategy não pode ser nulo.");
    }

    @Override
    public AiDecision decideForCurrentPlayer() {
        Game current = game.getGame();

        if (current == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }

        if (current.getStatus() != GameStatus.ACTIVE) {
            throw new InvalidTurnPhaseException("A partida já foi encerrada.");
        }

        if (!current.currentPlayer().isAi()) {
            throw new NotAiPlayerTurnException(
                    "O jogador atual não é controlado pelo computador."
            );
        }

        TurnState state = current.getTurn().getState();

        if (state instanceof DrawTileState) {
            return AiDecision.drawTile();
        }
        if (state instanceof PlaceTileState) {
            return decidePlacement();
        }
        if (state instanceof PlaceMeepleState) {
            return decideMeeple();
        }

        throw new AiDecisionException(
                "Fase de turno desconhecida: " + state.getClass().getSimpleName()
        );
    }

    private AiDecision decidePlacement() {
        List<Placement> legal = game.getLegalPlacements();

        if (legal.isEmpty()) {
            throw new AiDecisionException("Nenhuma colocação legal para a peça atual.");
        }

        Placement chosen = strategy.choosePlacement(legal);
        requireContained(legal, chosen);

        return AiDecision.placeTile(chosen);
    }

    private AiDecision decideMeeple() {
        List<FeatureRef> legal = game.getLegalMeepleFeatures();

        Optional<FeatureRef> chosen = strategy.chooseMeeple(legal);
        chosen.ifPresent(feature -> requireContained(legal, feature));

        return chosen
                .map(AiDecision::placeMeeple)
                .orElseGet(AiDecision::skipMeeple);
    }

    private static <T> void requireContained(List<T> legal, T chosen) {
        if (!legal.contains(chosen)) {
            throw new AiDecisionException(
                    "A estratégia escolheu uma jogada fora das opções legais: " + chosen
            );
        }
    }
}
