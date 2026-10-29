package com.carcassonne.domain.player;

import lombok.RequiredArgsConstructor;

/** GRASP Information Expert. */
@RequiredArgsConstructor
public class Player {

    private final String name;
    private final PlayerColor color;
    private int score;

    public void addScore(int points) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
