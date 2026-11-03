package com.carcassonne.application.port.in;

import com.carcassonne.domain.ai.AiDecision;

/** Port de entrada (hexagonal) dos jogadores controlados pelo computador. */
public interface AiInputPort {

    AiDecision decideForCurrentPlayer();
}
