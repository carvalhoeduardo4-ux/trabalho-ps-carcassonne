package com.carcassonne.application.port.in;

import com.carcassonne.domain.player.PlayerColor;
import lombok.Value;

@Value
public class PlayerSetup {
    String name;
    PlayerColor color;
}
