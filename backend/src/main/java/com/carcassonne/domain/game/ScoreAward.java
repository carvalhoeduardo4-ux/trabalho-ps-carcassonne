package com.carcassonne.domain.game;

import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScoreAward {
    private final int points;
    private final List<Player> recipients; // 1..*
    private final FeatureRegion region;

    public ScoreAward(int points, List<Player> recipients, FeatureRegion region) {
        this.points = points;
        this.recipients = recipients != null ? Collections.unmodifiableList(recipients) : Collections.emptyList();
        this.region = region;
    }

    public int getPoints() {
        return points;
    }

    public List<Player> getRecipients() {
        return recipients;
    }

    public FeatureRegion getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreAward that = (ScoreAward) o;
        return points == that.points && Objects.equals(recipients, that.recipients) && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, recipients, region);
    }

    @Override
    public String toString() {
        return "ScoreAward(" + points + " pts to " + recipients + ")";
    }
}
