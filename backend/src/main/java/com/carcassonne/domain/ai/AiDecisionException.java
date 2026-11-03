package com.carcassonne.domain.ai;

import lombok.experimental.StandardException;

/**
 * Defeito interno ao decidir a jogada da IA (estratégia devolveu opção
 * ilegal, fase inesperada etc.).
 *
 * Não é erro de regra: não estende GameActionException e é
 * respondida como 500, e não como 409.
 */
@StandardException
public class AiDecisionException extends RuntimeException {
}
