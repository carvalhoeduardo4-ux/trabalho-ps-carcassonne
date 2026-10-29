package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Rotation;
import lombok.Value;

@Value
public class Placement {
    Position position;
    Rotation rotation;
}
