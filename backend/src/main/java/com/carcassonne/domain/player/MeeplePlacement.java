package com.carcassonne.domain.player;

import com.carcassonne.domain.board.FeatureRef;
import java.util.Objects;

public class MeeplePlacement {
    private final Player player;
    private final FeatureRef feature;

    public MeeplePlacement(Player player, FeatureRef feature) {
        this.player = player;
        this.feature = feature;
    }

    public Player getPlayer() {
        return player;
    }

    public FeatureRef getFeature() {
        return feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeeplePlacement that = (MeeplePlacement) o;
        return Objects.equals(player, that.player) && Objects.equals(feature, that.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, feature);
    }

    @Override
    public String toString() {
        return "MeeplePlacement(" + player + ", " + feature + ")";
    }
}
