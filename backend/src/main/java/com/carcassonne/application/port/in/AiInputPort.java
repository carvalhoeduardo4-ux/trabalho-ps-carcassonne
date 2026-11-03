package com.carcassonne.application.port.in;

import com.carcassonne.domain.ai.AiDecision;

public interface AiInputPort {

    AiDecision decideForCurrentPlayer();
}
