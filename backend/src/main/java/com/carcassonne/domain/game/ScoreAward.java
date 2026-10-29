package com.carcassonne.domain.game;

import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.player.Player;
import lombok.Value;

import java.util.List;

@Value
public class ScoreAward {
    int points;
    List<Player> recipients; // 1..*
    FeatureRegion region;
}
