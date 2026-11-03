package com.carcassonne.api;

import com.carcassonne.api.dto.GameDtos.AiDecisionDto;
import com.carcassonne.application.port.in.AiInputPort;
import com.carcassonne.domain.ai.AiDecision;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST para jogadores controlados pelo computador.
 *
 * Devolve a jogada sorteada; o cliente a aplica pelos endpoints
 * existentes de {@link GameController} (/tile, /meeple, /skip, /draw).
 */
@RestController
@RequestMapping("/api/game/ai")
public class AiController {

    private final AiInputPort ai;

    public AiController(AiInputPort ai) {
        this.ai = ai;
    }

    @GetMapping("/decision")
    public AiDecisionDto decision() {
        AiDecision decision = ai.decideForCurrentPlayer();

        return switch (decision.getAction()) {
            case PLACE_TILE -> {
                Placement p = decision.getPlacement().orElseThrow();
                yield new AiDecisionDto("PLACE_TILE", p.getPosition().getX(), p.getPosition().getY(),
                        p.getRotation().name(), null);
            }
            case PLACE_MEEPLE -> {
                FeatureRef f = decision.getFeature().orElseThrow();
                yield new AiDecisionDto("PLACE_MEEPLE", f.getPosition().getX(), f.getPosition().getY(),
                        null, f.getFeatureId());
            }
            case SKIP_MEEPLE -> new AiDecisionDto("SKIP_MEEPLE", null, null, null, null);
            case DRAW_TILE -> new AiDecisionDto("DRAW_TILE", null, null, null, null);
        };
    }
}
