package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure Fabrication responsável por validar posições legais
 * para colocação de peças no tabuleiro.
 *
 * O tipo principal de uma borda é representado pela lane CENTER:
 *
 * CITY  -> CENTER = CITY
 * ROAD  -> CENTER = ROAD
 * FIELD -> CENTER = FIELD
 *
 * As lanes LEFT e RIGHT são utilizadas para conectividade interna
 * das features, principalmente campos separados por estradas,
 * e não alteram a compatibilidade básica entre duas bordas.
 */
@Component
public class TilePlacementRules {

    public boolean canPlace(
            Board board,
            Tile tile,
            Placement placement
    ) {
        if (board == null
                || tile == null
                || placement == null
                || placement.getPosition() == null
                || placement.getRotation() == null) {
            return false;
        }

        Position position =
                placement.getPosition();

        Rotation rotation =
                placement.getRotation();

        if (board.isOccupied(position)) {
            return false;
        }

        Map<Direction, PlacedTile> neighbors =
                board.neighbors(position);

        /*
         * Uma nova peça precisa tocar pelo menos
         * uma peça já existente.
         */
        if (neighbors.isEmpty()) {
            return false;
        }

        /*
         * Todas as bordas que tocam peças vizinhas
         * precisam ser compatíveis.
         */
        for (Map.Entry<Direction, PlacedTile> entry
                : neighbors.entrySet()) {

            Direction side =
                    entry.getKey();

            PlacedTile neighbor =
                    entry.getValue();

            if (!edgesMatch(
                    tile,
                    rotation,
                    side,
                    neighbor
            )) {
                return false;
            }
        }

        return true;
    }

    public List<Placement> legalPlacements(
            Board board,
            Tile tile
    ) {
        List<Placement> result =
                new ArrayList<>();

        if (board == null || tile == null) {
            return result;
        }

        for (Position position
                : board.frontierPositions()) {

            for (Rotation rotation
                    : Rotation.values()) {

                Placement candidate =
                        new Placement(
                                position,
                                rotation
                        );

                if (canPlace(
                        board,
                        tile,
                        candidate
                )) {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    /**
     * Verifica se duas bordas adjacentes possuem
     * o mesmo tipo principal de terreno.
     *
     * A lane CENTER representa o tipo da borda para
     * fins de colocação da peça.
     *
     * LEFT e RIGHT são deliberadamente ignoradas aqui:
     * elas representam subdivisões de features, especialmente
     * campos ao redor de estradas, e são tratadas por
     * FeatureConnectivity.
     */
    private boolean edgesMatch(
            Tile tile,
            Rotation rotation,
            Direction side,
            PlacedTile neighbor
    ) {
        Optional<TileFeature> mine =
                tile.featureAt(
                        side,
                        EdgeLane.CENTER,
                        rotation
                );

        Optional<TileFeature> theirs =
                neighbor.featureAt(
                        side.opposite(),
                        EdgeLane.CENTER
                );

        if (mine.isEmpty()
                || theirs.isEmpty()) {
            return false;
        }

        return mine.get().getType()
                == theirs.get().getType();
    }
}