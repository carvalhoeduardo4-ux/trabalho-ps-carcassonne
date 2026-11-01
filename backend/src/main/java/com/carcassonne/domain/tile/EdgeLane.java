package com.carcassonne.domain.tile;

/**
 * Faixa de uma borda, vista do centro da peça olhando para fora do lado.
 * Ao encostar duas peças, LEFT de uma toca RIGHT da outra.
 */
public enum EdgeLane {
    LEFT,
    CENTER,
    RIGHT;

    public EdgeLane opposite() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case CENTER -> CENTER;
        };
    }
}
