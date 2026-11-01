package com.carcassonne.domain.tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Fábrica de peças do jogo-base de Carcassonne.
 * O jogo-base é composto por 72 peças no total:
 * - 1 peça inicial (Starter Tile), posicionada na mesa em (0, 0);
 * - 71 peças de compra no monte (TileDeck).
 */
public final class StandardTiles {

    private StandardTiles() {}

    /**
     * Cria a peça inicial padrão: Cidade ao Norte, Estrada de Leste a Oeste, Campo ao Sul.
     */
    public static Tile createStarterTile() {
        List<TileFeature> features = List.of(
            new TileFeature(1, FeatureType.CITY, 0, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.LEFT),
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                new EdgeContact(Direction.NORTH, EdgeLane.RIGHT)
            )),
            new TileFeature(2, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            )),
            new TileFeature(3, FeatureType.FIELD, 0, List.of(
                new EdgeContact(Direction.SOUTH, EdgeLane.LEFT),
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                new EdgeContact(Direction.SOUTH, EdgeLane.RIGHT)
            ))
        );
        return new Tile("START", features, List.of());
    }

    /**
     * Cria as 71 peças padrão de compra do jogo-base (a peça inicial não faz parte deste monte).
     */
    public static List<Tile> createStandardDrawTiles() {
        List<Tile> tiles = new ArrayList<>(71);

        // A: Mosteiro com estrada ao sul (2 peças)
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("A", List.of(
                new TileFeature(1, FeatureType.MONASTERY, 0, List.of()),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // B: Mosteiro isolado no campo (4 peças)
        for (int i = 0; i < 4; i++) {
            tiles.add(new Tile("B", List.of(
                new TileFeature(1, FeatureType.MONASTERY, 0, List.of()),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // C: Cidade completa de 4 lados com escudo (1 peça)
        tiles.add(new Tile("C", List.of(
            new TileFeature(1, FeatureType.CITY, 1, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            ))
        )));

        // D: Cidade ao norte com estrada leste-oeste (cópia de compra: 3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("D", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                ))
            )));
        }

        // E: Cidade em 1 borda (norte) com campo nos demais lados (5 peças)
        for (int i = 0; i < 5; i++) {
            tiles.add(new Tile("E", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // F: Cidade em lados opostos conectada com escudo (1 peça) e sem escudo (1 peça) = 2 peças
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("F", List.of(
                new TileFeature(1, FeatureType.CITY, i == 0 ? 1 : 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                ))
            )));
        }

        // G: Cidade em lados opostos conectada reta (1 peça)
        tiles.add(new Tile("G", List.of(
            new TileFeature(1, FeatureType.CITY, 0, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
            )),
            new TileFeature(2, FeatureType.FIELD, 0, List.of(
                new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            ))
        )));

        // H: Cidade em lados opostos separadas (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("H", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                ))
            )));
        }

        // I: Cidade em 2 lados adjacentes separadas (2 peças)
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("I", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // J: Cidade em 1 borda com estrada em curva (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("J", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // K: Cidade em 1 borda com estrada em curva oposta (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("K", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                ))
            )));
        }

        // L: Cidade em 1 borda com bifurcação T de estrada (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("L", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(4, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // M: Cidade em canto (norte + leste) conectada com escudo (2 peças)
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("M", List.of(
                new TileFeature(1, FeatureType.CITY, 1, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // N: Cidade em canto (norte + leste) conectada sem escudo (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("N", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // O: Cidade em canto conectada com estrada curva (2 peças)
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("O", List.of(
                new TileFeature(1, FeatureType.CITY, 1, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // P: Cidade em canto conectada com estrada curva sem escudo (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("P", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // Q: Cidade em 3 lados conectada com escudo (1 peça)
        tiles.add(new Tile("Q", List.of(
            new TileFeature(1, FeatureType.CITY, 1, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            )),
            new TileFeature(2, FeatureType.FIELD, 0, List.of(
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
            ))
        )));

        // R: Cidade em 3 lados conectada sem escudo (3 peças)
        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile("R", List.of(
                new TileFeature(1, FeatureType.CITY, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                ))
            )));
        }

        // S: Cidade em 3 lados conectada com estrada e escudo (2 peças)
        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile("S", List.of(
                new TileFeature(1, FeatureType.CITY, 1, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                ))
            )));
        }

        // T: Cidade em 3 lados conectada com estrada sem escudo (1 peça)
        tiles.add(new Tile("T", List.of(
            new TileFeature(1, FeatureType.CITY, 0, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            )),
            new TileFeature(2, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
            ))
        )));

        // U: Estrada reta (8 peças)
        for (int i = 0; i < 8; i++) {
            tiles.add(new Tile("U", List.of(
                new TileFeature(1, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                ))
            )));
        }

        // V: Curva de estrada (9 peças)
        for (int i = 0; i < 9; i++) {
            tiles.add(new Tile("V", List.of(
                new TileFeature(1, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER),
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                ))
            )));
        }

        // W: Bifurcação em T de estradas (4 peças)
        for (int i = 0; i < 4; i++) {
            tiles.add(new Tile("W", List.of(
                new TileFeature(1, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
                )),
                new TileFeature(2, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.WEST, EdgeLane.CENTER)
                )),
                new TileFeature(3, FeatureType.ROAD, 0, List.of(
                    new EdgeContact(Direction.EAST, EdgeLane.CENTER)
                )),
                new TileFeature(4, FeatureType.FIELD, 0, List.of(
                    new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
                ))
            )));
        }

        // X: Cruzamento de 4 estradas (1 peça)
        tiles.add(new Tile("X", List.of(
            new TileFeature(1, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.NORTH, EdgeLane.CENTER)
            )),
            new TileFeature(2, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.EAST, EdgeLane.CENTER)
            )),
            new TileFeature(3, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.SOUTH, EdgeLane.CENTER)
            )),
            new TileFeature(4, FeatureType.ROAD, 0, List.of(
                new EdgeContact(Direction.WEST, EdgeLane.CENTER)
            ))
        )));

        return tiles;
    }
}
