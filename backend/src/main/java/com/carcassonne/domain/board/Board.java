package com.carcassonne.domain.board;

import com.carcassonne.domain.tile.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** GRASP Information Expert. Estrutura esparsa indexada por coordenadas. */
public class Board {

    private final Map<Position, PlacedTile> tiles = new HashMap<>();

    public boolean isOccupied(Position position) {
        return tileAt(position).isPresent();
    }

    public Optional<PlacedTile> tileAt(Position position) {
        if (position == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tiles.get(position));
    }

    /** Vizinhos ortogonais existentes, indexados pela direção a partir de position. */
    public Map<Direction, PlacedTile> neighbors(Position position) {
        Map<Direction, PlacedTile> result = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            tileAt(position.neighbor(d)).ifPresent(t -> result.put(d, t));
        }
        return result;
    }

    /** Peças existentes nas 8 posições ao redor (usado pelo mosteiro). */
    public List<PlacedTile> surrounding(Position position) {
        List<PlacedTile> result = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                tileAt(new Position(position.getX() + dx, position.getY() + dy)).ifPresent(result::add);
            }
        }
        return result;
    }

    /** Posições vazias adjacentes ortogonalmente a ao menos uma peça. */
    public Set<Position> frontierPositions() {
        Set<Position> frontier = new LinkedHashSet<>();
        for (Position p : tiles.keySet()) {
            for (Direction d : Direction.values()) {
                Position n = p.neighbor(d);
                if (!tiles.containsKey(n)) {
                    frontier.add(n);
                }
            }
        }
        return frontier;
    }

    public void place(PlacedTile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("Peça posicionada não pode ser nula");
        }
        if (tiles.containsKey(tile.getPosition())) {
            throw new IllegalStateException("Posição já ocupada: " + tile.getPosition());
        }
        tiles.put(tile.getPosition(), tile);
    }

    public Collection<PlacedTile> getTiles() {
        return Collections.unmodifiableCollection(tiles.values());
    }
}
