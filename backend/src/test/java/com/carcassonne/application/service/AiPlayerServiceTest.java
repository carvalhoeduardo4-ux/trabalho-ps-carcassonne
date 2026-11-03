package com.carcassonne.application.service;

import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.ai.AiAction;
import com.carcassonne.domain.ai.AiDecision;
import com.carcassonne.domain.ai.AiDecisionStrategy;
import com.carcassonne.domain.ai.RandomDecisionStrategy;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.game.GameStatus;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.player.PlayerType;
import com.carcassonne.domain.rules.CityScoringStrategy;
import com.carcassonne.domain.rules.FeatureConnectivity;
import com.carcassonne.domain.rules.FieldScoringStrategy;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.MonasteryScoringStrategy;
import com.carcassonne.domain.rules.RoadScoringStrategy;
import com.carcassonne.domain.rules.ScoringRules;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Rotation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPlayerServiceTest {

    private GameEngine newEngine() {
        FeatureConnectivity connectivity = new FeatureConnectivity();

        ScoringRules scoring = new ScoringRules(
                connectivity,
                new RoadScoringStrategy(),
                new CityScoringStrategy(),
                new MonasteryScoringStrategy(),
                new FieldScoringStrategy(connectivity)
        );

        return new GameEngine(
                new TilePlacementRules(),
                new MeeplePlacementRules(connectivity),
                scoring
        );
    }

    private AiPlayerService aiFor(GameEngine engine) {
        return new AiPlayerService(engine, new RandomDecisionStrategy(new Random(7)));
    }

    private List<PlayerSetup> players(PlayerType first, PlayerType second) {
        return List.of(
                new PlayerSetup("P1", PlayerColor.RED, first),
                new PlayerSetup("P2", PlayerColor.BLUE, second)
        );
    }

    /** Aplica a decisão pelos mesmos métodos usados por jogadores humanos. */
    private void apply(GameEngine engine, AiDecision decision) {
        switch (decision.getAction()) {
            case DRAW_TILE -> engine.drawTile();
            case PLACE_TILE -> engine.placeTile(decision.getPlacement().orElseThrow());
            case PLACE_MEEPLE -> engine.placeMeeple(decision.getFeature().orElseThrow());
            case SKIP_MEEPLE -> engine.skipMeeple();
        }
    }

    @Test
    void rejectsWhenNoGame() {
        GameEngine engine = newEngine();

        assertThrows(IllegalStateException.class, () -> aiFor(engine).decideForCurrentPlayer());
    }

    @Test
    void rejectsHumanTurn() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.HUMAN, PlayerType.AI));

        assertThrows(InvalidTurnPhaseException.class, () -> aiFor(engine).decideForCurrentPlayer());
    }

    @Test
    void drawPhaseReturnsDraw() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.HUMAN));

        assertEquals(AiAction.DRAW_TILE, aiFor(engine).decideForCurrentPlayer().getAction());
    }

    @Test
    void placementDecisionIsLegalAndAccepted() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.HUMAN));
        engine.drawTile();

        AiDecision decision = aiFor(engine).decideForCurrentPlayer();

        assertEquals(AiAction.PLACE_TILE, decision.getAction());
        Placement placement = decision.getPlacement().orElseThrow();
        assertTrue(engine.getLegalPlacements().contains(placement));

        engine.placeTile(placement);
    }

    @Test
    void meepleDecisionIsLegalOrSkip() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.HUMAN));
        AiPlayerService ai = aiFor(engine);

        engine.drawTile();
        apply(engine, ai.decideForCurrentPlayer());

        AiDecision decision = ai.decideForCurrentPlayer();

        assertTrue(decision.getAction() == AiAction.PLACE_MEEPLE
                || decision.getAction() == AiAction.SKIP_MEEPLE);
        decision.getFeature().ifPresent(
                feature -> assertTrue(engine.getLegalMeepleFeatures().contains(feature))
        );

        apply(engine, decision);
    }

    @Test
    void decisionDoesNotMutateState() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.HUMAN));
        engine.drawTile();
        AiPlayerService ai = aiFor(engine);

        int tilesBefore = engine.getGame().getBoard().getTiles().size();
        Object stateBefore = engine.getGame().getTurn().getState();

        for (int i = 0; i < 10; i++) {
            ai.decideForCurrentPlayer();
        }

        assertEquals(tilesBefore, engine.getGame().getBoard().getTiles().size());
        assertEquals(0, engine.getGame().getCurrentPlayerIndex());
        assertEquals(stateBefore, engine.getGame().getTurn().getState());
    }

    @Test
    void strategyReturningIllegalOptionIsRejected() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.HUMAN));
        engine.drawTile();

        AiDecisionStrategy broken = new AiDecisionStrategy() {
            @Override
            public Placement choosePlacement(List<Placement> legal) {
                return new Placement(new Position(99, 99), Rotation.DEG_0);
            }

            @Override
            public Optional<FeatureRef> chooseMeeple(List<FeatureRef> legal) {
                return Optional.empty();
            }
        };

        AiPlayerService ai = new AiPlayerService(engine, broken);

        assertThrows(IllegalStateException.class, ai::decideForCurrentPlayer);
    }

    @Test
    void twoAiPlayersFinishFullGame() {
        GameEngine engine = newEngine();
        engine.startGame(players(PlayerType.AI, PlayerType.AI));
        AiPlayerService ai = aiFor(engine);

        int decisions = 0;
        while (engine.getGame().getStatus() == GameStatus.ACTIVE) {
            apply(engine, ai.decideForCurrentPlayer());
            decisions++;
        }

        assertEquals(GameStatus.FINISHED, engine.getGame().getStatus());
        assertTrue(decisions > 0);
        assertTrue(engine.getGame().getBoard().getTiles().size() > 1);
        assertThrows(InvalidTurnPhaseException.class, ai::decideForCurrentPlayer);
    }
}
