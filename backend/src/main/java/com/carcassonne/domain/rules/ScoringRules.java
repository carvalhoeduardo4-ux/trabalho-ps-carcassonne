package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.tile.FeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pure Fabrication responsável por calcular premiações
 * de estruturas durante e ao final da partida.
 */
@Component
public class ScoringRules {

    private final FeatureConnectivity connectivity;

    private final Map<
            FeatureType,
            FeatureScoringStrategy
            > strategiesByType;

    public ScoringRules() {
        this(
                new FeatureConnectivity()
        );
    }

    private ScoringRules(
            FeatureConnectivity connectivity
    ) {
        this(
                connectivity,
                new RoadScoringStrategy(),
                new CityScoringStrategy(),
                new MonasteryScoringStrategy(),
                new FieldScoringStrategy(
                        connectivity
                )
        );
    }

    public ScoringRules(
            FeatureConnectivity connectivity,
            Map<
                    FeatureType,
                    FeatureScoringStrategy
                    > strategiesByType
    ) {
        this.connectivity =
                connectivity;

        this.strategiesByType =
                strategiesByType != null
                        ? strategiesByType
                        : Map.of();
    }

    @Autowired
    public ScoringRules(
            FeatureConnectivity connectivity,
            RoadScoringStrategy road,
            CityScoringStrategy city,
            MonasteryScoringStrategy monastery,
            FieldScoringStrategy field
    ) {
        this(
                connectivity,
                Map.of(
                        FeatureType.ROAD,
                        road,

                        FeatureType.CITY,
                        city,

                        FeatureType.MONASTERY,
                        monastery,

                        FeatureType.FIELD,
                        field
                )
        );
    }

    /**
     * Calcula as estruturas que foram completadas pela
     * peça recém-posicionada.
     *
     * Campos não pontuam durante a partida.
     */
    public List<ScoreAward> completedAwards(
            Board board,
            List<MeeplePlacement> meeples,
            PlacedTile placedTile
    ) {
        List<ScoreAward> awards =
                new ArrayList<>();

        Set<Set<FeatureRef>> seen =
                new HashSet<>();

        List<FeatureRef> candidates =
                new ArrayList<>();

        /*
         * Toda feature da peça colocada pode ter
         * completado uma estrutura.
         */
        placedTile
                .getTile()
                .getFeatures()
                .forEach(
                        feature ->
                                candidates.add(
                                        new FeatureRef(
                                                placedTile
                                                        .getPosition(),
                                                feature
                                                        .getId()
                                        )
                                )
                );

        /*
         * Mosteiros vizinhos também podem ter sido
         * completados pela nova peça.
         */
        for (PlacedTile around
                : board.surrounding(
                placedTile.getPosition()
        )) {

            around
                    .getTile()
                    .getFeatures()
                    .stream()
                    .filter(
                            feature ->
                                    feature.getType()
                                            == FeatureType.MONASTERY
                    )
                    .forEach(
                            feature ->
                                    candidates.add(
                                            new FeatureRef(
                                                    around
                                                            .getPosition(),
                                                    feature
                                                            .getId()
                                            )
                                    )
                    );
        }

        for (FeatureRef candidate
                : candidates) {

            FeatureType type =
                    featureTypeAt(
                            board,
                            candidate
                    );

            /*
             * Campos só pontuam no final da partida.
             */
            if (type == FeatureType.FIELD) {
                continue;
            }

            FeatureRegion region =
                    connectivity.connectedRegion(
                            board,
                            candidate
                    );

            /*
             * Uma estrutura pode aparecer várias vezes
             * entre os candidatos.
             */
            if (!markAsSeen(
                    seen,
                    region
            )) {
                continue;
            }

            if (!connectivity.isComplete(
                    board,
                    region
            )) {
                continue;
            }

            int points =
                    strategyFor(
                            region.getType()
                    ).completedScore(
                            board,
                            region
                    );

            awardRegion(
                    meeples,
                    region,
                    points
            ).ifPresent(
                    awards::add
            );
        }

        return awards;
    }

    /**
     * Calcula todas as premiações que ainda precisam
     * ser aplicadas ao final da partida.
     *
     * ROAD, CITY e MONASTERY:
     * somente estruturas incompletas que ainda possuem
     * meeples.
     *
     * FIELD:
     * todos os campos com meeple participam, pois campos
     * são pontuados exclusivamente no final da partida.
     */
    public List<ScoreAward> endGameAwards(
            Board board,
            List<MeeplePlacement> meeples
    ) {
        List<ScoreAward> awards =
                new ArrayList<>();

        if (meeples == null
                || meeples.isEmpty()) {
            return awards;
        }

        Set<Set<FeatureRef>> seen =
                new HashSet<>();

        /*
         * Partimos dos meeples, não de todas as features
         * do tabuleiro.
         *
         * Uma estrutura sem meeple não pode gerar pontos.
         */
        for (MeeplePlacement meeple
                : meeples) {

            FeatureRegion region =
                    connectivity.connectedRegion(
                            board,
                            meeple.getFeature()
                    );

            /*
             * A mesma região pode conter vários meeples,
             * principalmente depois que regiões antes
             * separadas são conectadas.
             */
            if (!markAsSeen(
                    seen,
                    region
            )) {
                continue;
            }

            /*
             * Estruturas completas de ROAD/CITY/MONASTERY
             * já deveriam ter sido pontuadas no turno em
             * que foram completadas.
             *
             * FIELD é diferente: campos nunca são
             * considerados completos durante a partida.
             */
            if (region.getType()
                    != FeatureType.FIELD
                    && connectivity.isComplete(
                    board,
                    region
            )) {

                continue;
            }

            int points =
                    strategyFor(
                            region.getType()
                    ).endGameScore(
                            board,
                            region
                    );

            /*
             * Mesmo um FIELD com zero cidades completas
             * precisa produzir um award de zero pontos.
             *
             * O Game usa o award também para devolver
             * os meeples da região.
             */
            awardRegion(
                    meeples,
                    region,
                    points
            ).ifPresent(
                    awards::add
            );
        }

        return awards;
    }

    /**
     * Cria a premiação de uma região considerando
     * maioria de meeples.
     *
     * Em caso de empate, todos os jogadores empatados
     * recebem a pontuação integral.
     */
    private Optional<ScoreAward> awardRegion(
            List<MeeplePlacement> meeples,
            FeatureRegion region,
            int points
    ) {
        Map<Player, Integer> counts =
                new LinkedHashMap<>();

        if (meeples != null) {
            for (MeeplePlacement meeple
                    : meeples) {

                if (region.contains(
                        meeple.getFeature()
                )) {
                    counts.merge(
                            meeple.getPlayer(),
                            1,
                            Integer::sum
                    );
                }
            }
        }

        if (counts.isEmpty()) {
            return Optional.empty();
        }

        int max =
                counts
                        .values()
                        .stream()
                        .max(
                                Integer::compare
                        )
                        .orElse(0);

        List<Player> recipients =
                counts
                        .entrySet()
                        .stream()
                        .filter(
                                entry ->
                                        entry.getValue()
                                                == max
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .toList();

        return Optional.of(
                new ScoreAward(
                        points,
                        recipients,
                        region
                )
        );
    }

    /**
     * Registra uma região usando seu conjunto de
     * FeatureRefs como identidade.
     *
     * Isso evita depender da ordem da lista retornada
     * pelo BFS.
     */
    private boolean markAsSeen(
            Set<Set<FeatureRef>> seen,
            FeatureRegion region
    ) {
        return seen.add(
                new HashSet<>(
                        region.getMembers()
                )
        );
    }

    private FeatureType featureTypeAt(
            Board board,
            FeatureRef feature
    ) {
        return board
                .tileAt(
                        feature.getPosition()
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Sem peça em "
                                                + feature
                                                .getPosition()
                                )
                )
                .feature(
                        feature.getFeatureId()
                )
                .getType();
    }

    private FeatureScoringStrategy strategyFor(
            FeatureType type
    ) {
        FeatureScoringStrategy strategy =
                strategiesByType.get(type);

        if (strategy == null) {
            throw new IllegalStateException(
                    "Nenhuma estratégia de pontuação "
                            + "registrada para "
                            + type
            );
        }

        return strategy;
    }
}