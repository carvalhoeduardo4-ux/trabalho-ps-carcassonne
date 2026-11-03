package com.carcassonne.application.service;

import com.carcassonne.application.port.in.AiInputPort;
import com.carcassonne.domain.ai.AiDecision;
import com.carcassonne.domain.ai.AiDecisionStrategy;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
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
 * GRASP Controller dos jogadores controlados pelo computador.
 *
 * Apenas decide: consulta as opções legais no GameEngine, delega a
 * escolha para a AiDecisionStrategy e devolve a jogada. A execução
 * continua pelos mesmos métodos usados por jogadores humanos.
 */
@Service
public class AiPlayerService implements AiInputPort {

    private final GameEngine engine;
    private final AiDecisionStrategy strategy;

    public AiPlayerService(GameEngine engine, AiDecisionStrategy strategy) {
        this.engine = Objects.requireNonNull(engine, "GameEngine não pode ser nulo.");
        this.strategy = Objects.requireNonNull(strategy, "AiDecisionStrategy não pode ser nulo.");
    }

    @Override
    public AiDecision decideForCurrentPlayer() {
        Game game = engine.getGame();

        if (game == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new InvalidTurnPhaseException("A partida já foi encerrada.");
        }

        if (!game.currentPlayer().isAi()) {
            throw new InvalidTurnPhaseException(
                    "O jogador atual não é controlado pelo computador."
            );
        }

        TurnState state = game.getTurn().getState();

        if (state instanceof DrawTileState) {
            return AiDecision.drawTile();
        }
        if (state instanceof PlaceTileState) {
            return decidePlacement();
        }
        if (state instanceof PlaceMeepleState) {
            return decideMeeple();
        }

        throw new IllegalStateException(
                "Fase de turno desconhecida: " + state.getClass().getSimpleName()
        );
    }

    private AiDecision decidePlacement() {
        List<Placement> legal = engine.getLegalPlacements();

        if (legal.isEmpty()) {
            // GameEngine.drawTile descarta peças sem colocação; não deveria ocorrer.
            throw new IllegalStateException("Nenhuma colocação legal para a peça atual.");
        }

        Placement chosen = strategy.choosePlacement(legal);
        requireContained(legal, chosen);

        return AiDecision.placeTile(chosen);
    }

    private AiDecision decideMeeple() {
        List<FeatureRef> legal = engine.getLegalMeepleFeatures();

        Optional<FeatureRef> chosen = strategy.chooseMeeple(legal);
        chosen.ifPresent(feature -> requireContained(legal, feature));

        return chosen
                .map(AiDecision::placeMeeple)
                .orElseGet(AiDecision::skipMeeple);
    }

    /** Protege contra estratégias com defeito: nunca devolver jogada ilegal. */
    private static <T> void requireContained(List<T> legal, T chosen) {
        if (!legal.contains(chosen)) {
            throw new IllegalStateException(
                    "A estratégia escolheu uma jogada fora das opções legais: " + chosen
            );
        }
    }
}
