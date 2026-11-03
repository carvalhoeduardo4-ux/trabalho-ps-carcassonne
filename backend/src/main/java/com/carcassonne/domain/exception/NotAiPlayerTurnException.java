package com.carcassonne.domain.exception;

import lombok.experimental.StandardException;

/** Decisão da IA pedida na vez de um jogador humano. */
@StandardException
public class NotAiPlayerTurnException extends GameActionException {
}
