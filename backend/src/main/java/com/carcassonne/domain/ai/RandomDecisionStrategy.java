package com.carcassonne.domain.ai;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * ConcreteStrategy: escolhas uniformemente aleatórias.
 *
 * Peças são sorteadas em dois estágios (posição, depois rotação válida
 * nessa posição) para que posições onde a peça encaixa em várias
 * rotações não sejam favorecidas.
 */
@Component
public class RandomDecisionStrategy implements AiDecisionStrategy {

    private final RandomGenerator random;

    @Autowired
    public RandomDecisionStrategy() {
        this(new Random());
    }

    public RandomDecisionStrategy(RandomGenerator random) {
        this.random = Objects.requireNonNull(
                random,
                "RandomGenerator não pode ser nulo."
        );
    }

    @Override
    public Placement choosePlacement(List<Placement> legal) {
        if (legal == null || legal.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não há colocações legais para escolher."
            );
        }

        Map<Position, List<Placement>> byPosition = legal.stream()
                .collect(Collectors.groupingBy(
                        Placement::getPosition,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<List<Placement>> positions =
                new ArrayList<>(byPosition.values());

        List<Placement> atPosition =
                positions.get(random.nextInt(positions.size()));

        return atPosition.get(random.nextInt(atPosition.size()));
    }

    @Override
    public Optional<FeatureRef> chooseMeeple(List<FeatureRef> legal) {
        if (legal == null || legal.isEmpty()) {
            return Optional.empty();
        }

        int choice = random.nextInt(legal.size() + 1);

        return choice == legal.size()
                ? Optional.empty()
                : Optional.of(legal.get(choice));
    }
}
