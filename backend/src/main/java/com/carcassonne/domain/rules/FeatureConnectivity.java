package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.FieldCityAdjacency;
import com.carcassonne.domain.tile.TileFeature;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Pure Fabrication.
 *
 * Identifica estruturas conectadas por busca em largura (BFS)
 * e relações entre campos e cidades.
 */
@Component
public class FeatureConnectivity {

    public FeatureRegion connectedRegion(
            Board board,
            FeatureRef start
    ) {
        PlacedTile startTile =
                board.tileAt(start.getPosition())
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sem peça em "
                                                + start.getPosition()
                                )
                        );

        FeatureType type =
                startTile.feature(
                        start.getFeatureId()
                ).getType();

        Set<FeatureRef> visited =
                new LinkedHashSet<>();

        Deque<FeatureRef> queue =
                new ArrayDeque<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            FeatureRef current =
                    queue.poll();

            PlacedTile tile =
                    board.tileAt(
                            current.getPosition()
                    ).orElseThrow();

            TileFeature currentFeature =
                    tile.feature(
                            current.getFeatureId()
                    );

            for (EdgeContact contact
                    : currentFeature.getEdgeContacts()) {

                Direction side =
                        contact.getSide()
                                .rotate(
                                        tile.getRotation()
                                );

                EdgeLane lane =
                        contact.getLane();

                Optional<PlacedTile> neighbor =
                        board.tileAt(
                                current.getPosition()
                                        .neighbor(side)
                        );

                if (neighbor.isEmpty()) {
                    continue;
                }

                /*
                 * EdgeLane é definida olhando do centro
                 * da peça em direção à borda.
                 *
                 * Por isso, entre peças adjacentes:
                 *
                 * LEFT   <-> RIGHT
                 * CENTER <-> CENTER
                 * RIGHT  <-> LEFT
                 *
                 * PlacedTile.featureAt() já considera
                 * a rotação da peça vizinha.
                 */
                Optional<TileFeature> other =
                        neighbor.get()
                                .featureAt(
                                        side.opposite(),
                                        lane.opposite()
                                );

                if (other.isEmpty()) {
                    continue;
                }

                if (other.get().getType() != type) {
                    continue;
                }

                FeatureRef ref =
                        new FeatureRef(
                                neighbor.get()
                                        .getPosition(),
                                other.get()
                                        .getId()
                        );

                if (visited.add(ref)) {
                    queue.add(ref);
                }
            }
        }

        return new FeatureRegion(
                type,
                new ArrayList<>(visited)
        );
    }

    /**
     * Cidades e estradas estão completas quando não
     * possuem pontas abertas.
     *
     * Mosteiro está completo quando possui oito peças
     * ao redor.
     *
     * Campos não possuem conceito de "completo" durante
     * a partida.
     */
    public boolean isComplete(
            Board board,
            FeatureRegion region
    ) {
        if (region.getType()
                == FeatureType.MONASTERY) {

            FeatureRef ref =
                    region.getMembers().get(0);

            return board
                    .surrounding(
                            ref.getPosition()
                    )
                    .size() == 8;
        }

        if (region.getType()
                == FeatureType.FIELD) {
            return false;
        }

        for (FeatureRef ref
                : region.getMembers()) {

            PlacedTile tile =
                    board.tileAt(
                            ref.getPosition()
                    ).orElseThrow();

            TileFeature feature =
                    tile.feature(
                            ref.getFeatureId()
                    );

            for (EdgeContact contact
                    : feature.getEdgeContacts()) {

                Direction side =
                        contact.getSide()
                                .rotate(
                                        tile.getRotation()
                                );

                if (!board.isOccupied(
                        ref.getPosition()
                                .neighbor(side)
                )) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Retorna todas as regiões de cidade adjacentes
     * a uma determinada região de campo.
     *
     * A relação campo-cidade é definida dentro de cada
     * Tile por FieldCityAdjacency.
     *
     * Quando uma cidade ocupa mais de uma peça, ela deve
     * aparecer apenas uma vez no resultado, mesmo que
     * vários membros do campo sejam adjacentes a ela.
     */
    public Set<FeatureRegion> adjacentCityRegions(
            Board board,
            FeatureRegion field
    ) {
        if (board == null) {
            throw new IllegalArgumentException(
                    "Board não pode ser nulo."
            );
        }

        if (field == null) {
            throw new IllegalArgumentException(
                    "Região não pode ser nula."
            );
        }

        if (field.getType()
                != FeatureType.FIELD) {
            throw new IllegalArgumentException(
                    "A região informada deve ser do tipo FIELD."
            );
        }

        Set<FeatureRegion> result =
                new LinkedHashSet<>();

        /*
         * FeatureRegion.equals() considera a List de
         * membros e, portanto, também sua ordem.
         *
         * A mesma cidade pode ser descoberta começando
         * por Features diferentes e o BFS pode produzir
         * outra ordem.
         *
         * Por isso a identidade lógica usada para
         * deduplicação é o conjunto de FeatureRefs.
         */
        Set<Set<FeatureRef>> seenCities =
                new HashSet<>();

        for (FeatureRef fieldRef
                : field.getMembers()) {

            PlacedTile placedTile =
                    board.tileAt(
                            fieldRef.getPosition()
                    ).orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            "Sem peça em "
                                                    + fieldRef
                                                    .getPosition()
                                    )
                    );

            TileFeature fieldFeature =
                    placedTile.feature(
                            fieldRef.getFeatureId()
                    );

            if (fieldFeature.getType()
                    != FeatureType.FIELD) {
                throw new IllegalArgumentException(
                        "Feature "
                                + fieldRef
                                + " não pertence a um campo."
                );
            }

            for (FieldCityAdjacency adjacency
                    : placedTile
                    .getTile()
                    .getFieldCityAdjacencies()) {

                /*
                 * A relação pertence ao campo que estamos
                 * analisando nesta peça?
                 */
                if (adjacency
                        .getField()
                        .getId()
                        != fieldRef.getFeatureId()) {
                    continue;
                }

                FeatureRef cityRef =
                        new FeatureRef(
                                fieldRef.getPosition(),
                                adjacency
                                        .getCity()
                                        .getId()
                        );

                FeatureRegion cityRegion =
                        connectedRegion(
                                board,
                                cityRef
                        );

                if (cityRegion.getType()
                        != FeatureType.CITY) {
                    throw new IllegalStateException(
                            "FieldCityAdjacency deve apontar "
                                    + "para uma feature CITY."
                    );
                }

                Set<FeatureRef> cityIdentity =
                        new HashSet<>(
                                cityRegion.getMembers()
                        );

                if (seenCities.add(
                        cityIdentity
                )) {
                    result.add(
                            cityRegion
                    );
                }
            }
        }

        return result;
    }
}