package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.TileFeature;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Pure Fabrication. Identifica estruturas conectadas por busca em largura (BFS). */
@Component
public class FeatureConnectivity {

    public FeatureRegion connectedRegion(Board board, FeatureRef start) {
        PlacedTile startTile = board.tileAt(start.getPosition())
                .orElseThrow(() -> new IllegalArgumentException("Sem peça em " + start.getPosition()));
        FeatureType type = startTile.feature(start.getFeatureId()).getType();

        Set<FeatureRef> visited = new LinkedHashSet<>();
        Deque<FeatureRef> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            FeatureRef current = queue.poll();
            PlacedTile tile = board.tileAt(current.getPosition()).orElseThrow();
            for (EdgeContact contact : tile.feature(current.getFeatureId()).getEdgeContacts()) {
                Direction side = contact.getSide().rotate(tile.getRotation());
                Optional<PlacedTile> neighbor = board.tileAt(current.getPosition().neighbor(side));
                if (neighbor.isEmpty()) {
                    continue;
                }
                Optional<TileFeature> other = neighbor.get().edgeFeature(side.opposite());
                if (other.isPresent() && other.get().getType() == type) {
                    FeatureRef ref = new FeatureRef(neighbor.get().getPosition(), other.get().getId());
                    if (visited.add(ref)) {
                        queue.add(ref);
                    }
                }
            }
        }
        return new FeatureRegion(type, new ArrayList<>(visited));
    }

    /** Cidades/estradas: nenhuma ponta aberta. Mosteiro: 8 peças ao redor. */
    public boolean isComplete(Board board, FeatureRegion region) {
        if (region.getType() == FeatureType.MONASTERY) {
            FeatureRef ref = region.getMembers().get(0);
            return board.surrounding(ref.getPosition()).size() == 8;
        }
        if (region.getType() == FeatureType.FIELD) {
            return false;
        }
        for (FeatureRef ref : region.getMembers()) {
            PlacedTile tile = board.tileAt(ref.getPosition()).orElseThrow();
            for (EdgeContact contact : tile.feature(ref.getFeatureId()).getEdgeContacts()) {
                Direction side = contact.getSide().rotate(tile.getRotation());
                if (!board.isOccupied(ref.getPosition().neighbor(side))) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<FeatureRegion> adjacentCityRegions(Board board, FeatureRegion field) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
