package com.carcassonne.domain.ai;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Direction;
import com.carcassonne.domain.tile.EdgeContact;
import com.carcassonne.domain.tile.EdgeLane;
import com.carcassonne.domain.tile.FeatureType;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomDecisionStrategyTest {

    private static final Position A = new Position(1, 0);
    private static final Position B = new Position(0, -1);
    private static final Position C = new Position(-1, 0);

    /** Gerador que devolve, em ordem, os valores informados em nextInt(bound). */
    private static RandomGenerator fixed(int... values) {
        Iterator<Integer> it = Arrays.stream(values).iterator();
        return new RandomGenerator() {
            @Override
            public long nextLong() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int nextInt(int bound) {
                return it.next();
            }
        };
    }

    private static List<Placement> fivePlacements() {
        return List.of(
                new Placement(A, Rotation.DEG_0),
                new Placement(A, Rotation.DEG_90),
                new Placement(B, Rotation.DEG_180),
                new Placement(C, Rotation.DEG_270),
                new Placement(C, Rotation.DEG_0)
        );
    }

    // =====================================================
    // PLACE TILE
    // =====================================================

    @Test
    @DisplayName("Sempre escolhe uma colocação contida na lista recebida")
    void choosesOnlyFromLegalList() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));
        List<Placement> legal = fivePlacements();

        for (int i = 0; i < 1000; i++) {
            assertTrue(legal.contains(strategy.choosePlacement(legal)));
        }
    }

    @Test
    @DisplayName("Nunca escolhe uma rotação que não está entre as colocações legais")
    void neverChoosesRotationAbsentFromList() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));
        List<Placement> legal = List.of(
                new Placement(A, Rotation.DEG_90),
                new Placement(B, Rotation.DEG_90),
                new Placement(B, Rotation.DEG_270)
        );

        for (int i = 0; i < 1000; i++) {
            assertNotEquals(Rotation.DEG_0, strategy.choosePlacement(legal).getRotation());
        }
    }

    @Test
    @DisplayName("Peça que não encaixa em DEG_0: a IA escolhe uma rotação válida pelas regras reais")
    void rotationScenarioWithRealRules() {
        Board board = new Board();
        board.place(new PlacedTile(
                StandardTiles.createStarterTile(),
                new Placement(new Position(0, 0), Rotation.DEG_0)
        ));

        // Estrada reta norte-sul: precisa girar para seguir a estrada leste-oeste da peça inicial.
        Tile straight = new Tile(List.of(
                new TileFeature(1, FeatureType.ROAD, 0, List.of(
                        new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                        new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                        new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                        new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
        ));

        TilePlacementRules rules = new TilePlacementRules();
        List<Placement> legal = rules.legalPlacements(board, straight);

        assertFalse(legal.isEmpty());
        assertFalse(legal.contains(new Placement(new Position(1, 0), Rotation.DEG_0)));

        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));
        for (int i = 0; i < 200; i++) {
            Placement chosen = strategy.choosePlacement(legal);
            assertTrue(rules.canPlace(board, straight, chosen));
        }
    }

    @Test
    @DisplayName("Sorteio em dois estágios dá a mesma chance a cada posição")
    void twoStageSelectionIsFairAcrossPositions() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));
        List<Placement> legal = List.of(
                new Placement(A, Rotation.DEG_0),
                new Placement(A, Rotation.DEG_90),
                new Placement(A, Rotation.DEG_180),
                new Placement(A, Rotation.DEG_270),
                new Placement(B, Rotation.DEG_90)
        );

        int iterations = 10_000;
        int atB = 0;
        for (int i = 0; i < iterations; i++) {
            if (strategy.choosePlacement(legal).getPosition().equals(B)) {
                atB++;
            }
        }

        double ratio = atB / (double) iterations;
        assertTrue(ratio > 0.47 && ratio < 0.53, "Frequência de B: " + ratio);
    }

    @Test
    @DisplayName("Primeiro sorteia a posição, depois a rotação nessa posição")
    void exactPickWithFixedGenerator() {
        List<Placement> legal = List.of(
                new Placement(A, Rotation.DEG_0),
                new Placement(A, Rotation.DEG_90),
                new Placement(B, Rotation.DEG_180)
        );

        RandomDecisionStrategy strategy = new RandomDecisionStrategy(fixed(0, 1));

        assertEquals(new Placement(A, Rotation.DEG_90), strategy.choosePlacement(legal));
    }

    @Test
    @DisplayName("Lista de colocações vazia ou nula é rejeitada")
    void emptyPlacementListThrows() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));

        assertThrows(IllegalArgumentException.class, () -> strategy.choosePlacement(List.of()));
        assertThrows(IllegalArgumentException.class, () -> strategy.choosePlacement(null));
    }

    // =====================================================
    // MEEPLE
    // =====================================================

    @Test
    @DisplayName("Sem features legais, a IA não coloca meeple")
    void meepleEmptyListSkips() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));

        assertEquals(Optional.empty(), strategy.chooseMeeple(List.of()));
        assertEquals(Optional.empty(), strategy.chooseMeeple(null));
    }

    @Test
    @DisplayName("O último índice sorteado representa não colocar meeple")
    void meepleSkipIsAnOption() {
        List<FeatureRef> legal = List.of(new FeatureRef(A, 1), new FeatureRef(A, 2));

        RandomDecisionStrategy strategy = new RandomDecisionStrategy(fixed(2));

        assertEquals(Optional.empty(), strategy.chooseMeeple(legal));
    }

    @Test
    @DisplayName("Índice sorteado dentro da lista escolhe a feature correspondente")
    void meeplePicksFeature() {
        List<FeatureRef> legal = List.of(new FeatureRef(A, 1), new FeatureRef(A, 2));

        RandomDecisionStrategy strategy = new RandomDecisionStrategy(fixed(1));

        assertEquals(Optional.of(new FeatureRef(A, 2)), strategy.chooseMeeple(legal));
    }

    @Test
    @DisplayName("Todas as saídas de meeple são alcançáveis e sempre legais")
    void meepleAllOutcomesReachable() {
        RandomDecisionStrategy strategy = new RandomDecisionStrategy(new Random(42));
        List<FeatureRef> legal = List.of(new FeatureRef(A, 1), new FeatureRef(A, 2));

        Set<Optional<FeatureRef>> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Optional<FeatureRef> chosen = strategy.chooseMeeple(legal);
            chosen.ifPresent(f -> assertTrue(legal.contains(f)));
            seen.add(chosen);
        }

        assertEquals(3, seen.size());
    }
}
