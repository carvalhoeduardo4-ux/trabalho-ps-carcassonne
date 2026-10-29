package com.carcassonne.application.port.in;

import com.carcassonne.domain.player.PlayerColor;
import java.util.Objects;

public class PlayerSetup {
    private final String name;
    private final PlayerColor color;

    public PlayerSetup(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerSetup that = (PlayerSetup) o;
        return Objects.equals(name, that.name) && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return "PlayerSetup(" + name + ", " + color + ")";
    }
}
