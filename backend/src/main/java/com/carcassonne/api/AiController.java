package com.carcassonne.api;

import com.carcassonne.api.dto.GameDtos.AiDecisionDto;
import com.carcassonne.api.dto.GameDtos.FeatureRefDto;
import com.carcassonne.api.dto.GameDtos.PlacementDto;
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
        return new AiDecisionDto(
                decision.getAction().name(),
                decision.getPlacement().map(this::placementDto).orElse(null),
                decision.getFeature().map(this::featureDto).orElse(null));
    }

    private PlacementDto placementDto(Placement p) {
        return new PlacementDto(p.getPosition().getX(), p.getPosition().getY(), p.getRotation().name());
    }

    private FeatureRefDto featureDto(FeatureRef f) {
        return new FeatureRefDto(f.getPosition().getX(), f.getPosition().getY(), f.getFeatureId());
    }
}
