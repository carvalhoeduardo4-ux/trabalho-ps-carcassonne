package com.carcassonne.domain.rules;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRegion;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FieldScoringStrategy implements FeatureScoringStrategy {

    private final FeatureConnectivity connectivity;

    public FieldScoringStrategy(
            FeatureConnectivity connectivity
    ) {
        this.connectivity =
                Objects.requireNonNull(
                        connectivity,
                        "connectivity"
                );
    }

    /**
     * Campos não pontuam durante a partida.
     */
    @Override
    public int completedScore(
            Board board,
            FeatureRegion region
    ) {
        return 0;
    }

    /**
     * No fim da partida, um campo vale
     * 3 pontos por cidade completa distinta adjacente.
     */
    @Override
    public int endGameScore(
            Board board,
            FeatureRegion region
    ) {
        long completedCities =
                connectivity
                        .adjacentCityRegions(
                                board,
                                region
                        )
                        .stream()
                        .filter(
                                city ->
                                        connectivity
                                                .isComplete(
                                                        board,
                                                        city
                                                )
                        )
                        .count();

        return Math.toIntExact(
                completedCities * 3
        );
    }
}