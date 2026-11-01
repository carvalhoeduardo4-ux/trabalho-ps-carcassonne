package com.carcassonne.domain.tile;

public enum Rotation {
    DEG_0,
    DEG_90,
    DEG_180,
    DEG_270;

    /** Número de giros de 90 graus no sentido horário. */
    public int quarterTurns() {
        return ordinal();
    }
}
