package com.carcassonne.domain.tile;

import lombok.Value;

@Value
public class EdgeContact {
    Direction side;
    EdgeLane lane;
}
