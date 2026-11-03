package com.carcassonne.application.port.in;

import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.player.PlayerType;
import java.util.Objects;

public class PlayerSetup {
    private final String name;
    private final PlayerColor color;
    private final PlayerType type;

    public PlayerSetup(String name, PlayerColor color) {
        this(name, color, PlayerType.HUMAN);
    }

    public PlayerSetup(String name, PlayerColor color, PlayerType type) {
        this.name = name;
        this.color = color;
        this.type = type != null ? type : PlayerType.HUMAN;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public PlayerType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerSetup that = (PlayerSetup) o;
        return Objects.equals(name, that.name) && color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, type);
    }

    @Override
    public String toString() {
        return "PlayerSetup(" + name + ", " + color + ", " + type + ")";
    }
}
