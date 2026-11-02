package com.carcassonne.domain.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Fábrica de peças do jogo-base de Carcassonne.
 *
 * O jogo-base possui 72 peças no total:
 * - 1 peça inicial, colocada em (0, 0);
 * - 71 peças de compra no TileDeck.
 *
 * Convenção das bordas:
 * - CENTER representa o tipo principal da borda e é usado para validar encaixe;
 * - em bordas ROAD, LEFT/RIGHT representam os campos dos dois lados da estrada;
 * - em bordas FIELD, a mesma feature de campo ocupa LEFT/CENTER/RIGHT;
 * - bordas CITY não possuem passagem de campo para a peça vizinha.
 *
 * EdgeLane é observada do centro da peça olhando para fora da borda.
 */
public final class StandardTiles {

    private StandardTiles() {}

    /**
     * Peça inicial padrão, topologicamente equivalente à peça D:
     * cidade ao norte e estrada leste-oeste.
     */
    public static Tile createStarterTile() {
        return tileD("START");
    }

    /** Cria as 71 peças de compra do jogo-base. */
    public static List<Tile> createStandardDrawTiles() {
        List<Tile> tiles = new ArrayList<>(71);

        addCopies(tiles, 2, StandardTiles::tileA);
        addCopies(tiles, 4, StandardTiles::tileB);
        addCopies(tiles, 1, StandardTiles::tileC);

        // Existem 4 peças D no jogo-base; uma delas é a peça inicial.
        addCopies(tiles, 3, () -> tileD("D"));

        addCopies(tiles, 5, StandardTiles::tileE);

        // F: duas cópias, uma com escudo e outra sem escudo.
        tiles.add(tileF(1));
        tiles.add(tileF(0));

        addCopies(tiles, 1, StandardTiles::tileG);
        addCopies(tiles, 3, StandardTiles::tileH);
        addCopies(tiles, 2, StandardTiles::tileI);
        addCopies(tiles, 3, StandardTiles::tileJ);
        addCopies(tiles, 3, StandardTiles::tileK);
        addCopies(tiles, 3, StandardTiles::tileL);
        addCopies(tiles, 2, StandardTiles::tileM);
        addCopies(tiles, 3, StandardTiles::tileN);
        addCopies(tiles, 2, StandardTiles::tileO);
        addCopies(tiles, 3, StandardTiles::tileP);
        addCopies(tiles, 1, StandardTiles::tileQ);
        addCopies(tiles, 3, StandardTiles::tileR);
        addCopies(tiles, 2, StandardTiles::tileS);
        addCopies(tiles, 1, StandardTiles::tileT);
        addCopies(tiles, 8, StandardTiles::tileU);
        addCopies(tiles, 9, StandardTiles::tileV);
        addCopies(tiles, 4, StandardTiles::tileW);
        addCopies(tiles, 1, StandardTiles::tileX);

        return tiles;
    }

    // ---------------------------------------------------------------------
    // A-X
    // ---------------------------------------------------------------------

    /** A: mosteiro com estrada ao sul. O campo contorna o mosteiro. */
    private static Tile tileA() {
        TileFeature monastery = monastery(1);
        TileFeature road = road(2, Direction.SOUTH);
        TileFeature field = field(3, combine(
                fullFieldEdges(
                        Direction.NORTH,
                        Direction.EAST,
                        Direction.WEST
                ),
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.SOUTH, EdgeLane.RIGHT)
        ));

        return tile("A", List.of(monastery, road, field));
    }

    /** B: mosteiro isolado em um único campo. */
    private static Tile tileB() {
        TileFeature monastery = monastery(1);
        TileFeature field = field(2, fullFieldEdges(
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        ));

        return tile("B", List.of(monastery, field));
    }

    /** C: cidade ocupando as quatro bordas, com um escudo. */
    private static Tile tileC() {
        TileFeature city = city(
                1,
                1,
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        );

        return tile("C", List.of(city));
    }

    /** D/START: cidade ao norte e estrada reta leste-oeste. */
    private static Tile tileD(String id) {
        TileFeature city = city(1, 0, Direction.NORTH);
        TileFeature road = road(2, Direction.EAST, Direction.WEST);

        // Mantém o id 3 para o campo ao sul, já existente no modelo anterior.
        TileFeature southField = field(3, combine(
                fullFieldEdges(Direction.SOUTH),
                lane(Direction.EAST, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        TileFeature northField = field(4, List.of(
                lane(Direction.EAST, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        return tile(
                id,
                List.of(city, road, southField, northField),
                adjacency(northField, city)
        );
    }

    /** E: cidade ao norte e um único campo nos demais lados. */
    private static Tile tileE() {
        TileFeature city = city(1, 0, Direction.NORTH);
        TileFeature field = field(2, fullFieldEdges(
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        ));

        return tile(
                "E",
                List.of(city, field),
                adjacency(field, city)
        );
    }

    /** F: cidade conectada entre leste-oeste, separando campo norte e sul. */
    private static Tile tileF(int shields) {
        TileFeature city = city(
                1,
                shields,
                Direction.EAST,
                Direction.WEST
        );

        TileFeature northField = field(
                2,
                fullFieldEdges(Direction.NORTH)
        );

        TileFeature southField = field(
                3,
                fullFieldEdges(Direction.SOUTH)
        );

        return tile(
                "F",
                List.of(city, northField, southField),
                adjacency(northField, city),
                adjacency(southField, city)
        );
    }

    /** G: cidade conectada entre norte-sul, separando campo leste e oeste. */
    private static Tile tileG() {
        TileFeature city = city(
                1,
                0,
                Direction.NORTH,
                Direction.SOUTH
        );

        TileFeature eastField = field(
                2,
                fullFieldEdges(Direction.EAST)
        );

        TileFeature westField = field(
                3,
                fullFieldEdges(Direction.WEST)
        );

        return tile(
                "G",
                List.of(city, eastField, westField),
                adjacency(eastField, city),
                adjacency(westField, city)
        );
    }

    /** H: duas cidades opostas e separadas, com um campo entre elas. */
    private static Tile tileH() {
        TileFeature eastCity = city(1, 0, Direction.EAST);
        TileFeature westCity = city(2, 0, Direction.WEST);
        TileFeature field = field(3, fullFieldEdges(
                Direction.NORTH,
                Direction.SOUTH
        ));

        return tile(
                "H",
                List.of(eastCity, westCity, field),
                adjacency(field, eastCity),
                adjacency(field, westCity)
        );
    }

    /** I: duas cidades adjacentes e separadas, com um campo comum. */
    private static Tile tileI() {
        TileFeature northCity = city(1, 0, Direction.NORTH);
        TileFeature eastCity = city(2, 0, Direction.EAST);
        TileFeature field = field(3, fullFieldEdges(
                Direction.SOUTH,
                Direction.WEST
        ));

        return tile(
                "I",
                List.of(northCity, eastCity, field),
                adjacency(field, northCity),
                adjacency(field, eastCity)
        );
    }

    /** J: cidade ao norte e estrada curva leste-sul. */
    private static Tile tileJ() {
        TileFeature city = city(1, 0, Direction.NORTH);
        TileFeature road = road(2, Direction.EAST, Direction.SOUTH);

        // Campo externo à curva, junto à cidade.
        TileFeature outerField = field(3, combine(
                fullFieldEdges(Direction.WEST),
                lane(Direction.EAST, EdgeLane.LEFT),
                lane(Direction.SOUTH, EdgeLane.RIGHT)
        ));

        // Campo interno da curva.
        TileFeature innerField = field(4, List.of(
                lane(Direction.EAST, EdgeLane.RIGHT),
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        return tile(
                "J",
                List.of(city, road, outerField, innerField),
                adjacency(outerField, city)
        );
    }

    /** K: cidade ao norte e estrada curva sul-oeste. */
    private static Tile tileK() {
        TileFeature city = city(1, 0, Direction.NORTH);
        TileFeature road = road(2, Direction.SOUTH, Direction.WEST);

        // Campo externo à curva, junto à cidade.
        TileFeature outerField = field(3, combine(
                fullFieldEdges(Direction.EAST),
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        // Campo interno da curva.
        TileFeature innerField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        return tile(
                "K",
                List.of(city, road, outerField, innerField),
                adjacency(outerField, city)
        );
    }

    /** L: cidade ao norte e bifurcação em T de estrada para E/S/W. */
    private static Tile tileL() {
        TileFeature city = city(1, 0, Direction.NORTH);
        TileFeature road = road(
                2,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        );

        TileFeature northField = field(3, List.of(
                lane(Direction.EAST, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        TileFeature southEastField = field(4, List.of(
                lane(Direction.EAST, EdgeLane.RIGHT),
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        TileFeature southWestField = field(5, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        return tile(
                "L",
                List.of(
                        city,
                        road,
                        northField,
                        southEastField,
                        southWestField
                ),
                adjacency(northField, city)
        );
    }

    /** M: cidade conectada no canto norte-leste, com escudo. */
    private static Tile tileM() {
        TileFeature city = city(
                1,
                1,
                Direction.NORTH,
                Direction.EAST
        );

        TileFeature field = field(2, fullFieldEdges(
                Direction.SOUTH,
                Direction.WEST
        ));

        return tile(
                "M",
                List.of(city, field),
                adjacency(field, city)
        );
    }

    /** N: cidade conectada no canto norte-leste, sem escudo. */
    private static Tile tileN() {
        TileFeature city = city(
                1,
                0,
                Direction.NORTH,
                Direction.EAST
        );

        TileFeature field = field(2, fullFieldEdges(
                Direction.SOUTH,
                Direction.WEST
        ));

        return tile(
                "N",
                List.of(city, field),
                adjacency(field, city)
        );
    }

    /** O: cidade norte-leste com escudo e estrada curva sul-oeste. */
    private static Tile tileO() {
        TileFeature city = city(
                1,
                1,
                Direction.NORTH,
                Direction.EAST
        );

        TileFeature road = road(
                2,
                Direction.SOUTH,
                Direction.WEST
        );

        TileFeature outerField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        TileFeature innerField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        return tile(
                "O",
                List.of(city, road, outerField, innerField),
                adjacency(outerField, city)
        );
    }

    /** P: cidade norte-leste sem escudo e estrada curva sul-oeste. */
    private static Tile tileP() {
        TileFeature city = city(
                1,
                0,
                Direction.NORTH,
                Direction.EAST
        );

        TileFeature road = road(
                2,
                Direction.SOUTH,
                Direction.WEST
        );

        TileFeature outerField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        TileFeature innerField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        return tile(
                "P",
                List.of(city, road, outerField, innerField),
                adjacency(outerField, city)
        );
    }

    /** Q: cidade conectada em N/E/W, com escudo, e campo ao sul. */
    private static Tile tileQ() {
        TileFeature city = city(
                1,
                1,
                Direction.NORTH,
                Direction.EAST,
                Direction.WEST
        );

        TileFeature field = field(
                2,
                fullFieldEdges(Direction.SOUTH)
        );

        return tile(
                "Q",
                List.of(city, field),
                adjacency(field, city)
        );
    }

    /** R: cidade conectada em N/E/W, sem escudo, e campo ao sul. */
    private static Tile tileR() {
        TileFeature city = city(
                1,
                0,
                Direction.NORTH,
                Direction.EAST,
                Direction.WEST
        );

        TileFeature field = field(
                2,
                fullFieldEdges(Direction.SOUTH)
        );

        return tile(
                "R",
                List.of(city, field),
                adjacency(field, city)
        );
    }

    /** S: cidade N/E/W com escudo e estrada ao sul. */
    private static Tile tileS() {
        TileFeature city = city(
                1,
                1,
                Direction.NORTH,
                Direction.EAST,
                Direction.WEST
        );

        TileFeature road = road(2, Direction.SOUTH);

        TileFeature eastField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        TileFeature westField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT)
        ));

        return tile(
                "S",
                List.of(city, road, eastField, westField),
                adjacency(eastField, city),
                adjacency(westField, city)
        );
    }

    /** T: cidade N/E/W sem escudo e estrada ao sul. */
    private static Tile tileT() {
        TileFeature city = city(
                1,
                0,
                Direction.NORTH,
                Direction.EAST,
                Direction.WEST
        );

        TileFeature road = road(2, Direction.SOUTH);

        TileFeature eastField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        TileFeature westField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT)
        ));

        return tile(
                "T",
                List.of(city, road, eastField, westField),
                adjacency(eastField, city),
                adjacency(westField, city)
        );
    }

    /** U: estrada reta norte-sul, separando campo leste e oeste. */
    private static Tile tileU() {
        TileFeature road = road(
                1,
                Direction.NORTH,
                Direction.SOUTH
        );

        // Mantém o id 2 para um dos campos existentes no modelo anterior.
        TileFeature eastField = field(2, combine(
                fullFieldEdges(Direction.EAST),
                lane(Direction.NORTH, EdgeLane.RIGHT),
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        TileFeature westField = field(3, combine(
                fullFieldEdges(Direction.WEST),
                lane(Direction.NORTH, EdgeLane.LEFT),
                lane(Direction.SOUTH, EdgeLane.RIGHT)
        ));

        return tile(
                "U",
                List.of(road, eastField, westField)
        );
    }

    /** V: estrada curva sul-oeste, separando campo interno e externo. */
    private static Tile tileV() {
        TileFeature road = road(
                1,
                Direction.SOUTH,
                Direction.WEST
        );

        // Mantém o id 2 para o campo externo já representado anteriormente.
        TileFeature outerField = field(2, combine(
                fullFieldEdges(
                        Direction.NORTH,
                        Direction.EAST
                ),
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        TileFeature innerField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        return tile(
                "V",
                List.of(road, outerField, innerField)
        );
    }

    /** W: bifurcação em T para S/W/E, separando três campos. */
    private static Tile tileW() {
        TileFeature road = road(
                1,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        );

        TileFeature southEastField = field(2, List.of(
                lane(Direction.SOUTH, EdgeLane.LEFT),
                lane(Direction.EAST, EdgeLane.RIGHT)
        ));

        TileFeature southWestField = field(3, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        // Preserva o id 4 do campo norte existente desde o PR3.
        TileFeature northField = field(4, combine(
                fullFieldEdges(Direction.NORTH),
                lane(Direction.EAST, EdgeLane.LEFT),
                lane(Direction.WEST, EdgeLane.RIGHT)
        ));

        return tile(
                "W",
                List.of(
                        road,
                        southEastField,
                        southWestField,
                        northField
                )
        );
    }

    /** X: cruzamento de quatro estradas, separando quatro campos. */
    private static Tile tileX() {
        TileFeature road = road(
                1,
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        );

        TileFeature northEastField = field(2, List.of(
                lane(Direction.NORTH, EdgeLane.RIGHT),
                lane(Direction.EAST, EdgeLane.LEFT)
        ));

        TileFeature southEastField = field(3, List.of(
                lane(Direction.EAST, EdgeLane.RIGHT),
                lane(Direction.SOUTH, EdgeLane.LEFT)
        ));

        TileFeature southWestField = field(4, List.of(
                lane(Direction.SOUTH, EdgeLane.RIGHT),
                lane(Direction.WEST, EdgeLane.LEFT)
        ));

        TileFeature northWestField = field(5, List.of(
                lane(Direction.WEST, EdgeLane.RIGHT),
                lane(Direction.NORTH, EdgeLane.LEFT)
        ));

        return tile(
                "X",
                List.of(
                        road,
                        northEastField,
                        southEastField,
                        southWestField,
                        northWestField
                )
        );
    }

    // ---------------------------------------------------------------------
    // Helpers de construção
    // ---------------------------------------------------------------------

    private static Tile tile(
            String id,
            List<TileFeature> features,
            FieldCityAdjacency... adjacencies
    ) {
        return new Tile(
                id,
                features,
                List.of(adjacencies)
        );
    }

    private static TileFeature monastery(int id) {
        return new TileFeature(
                id,
                FeatureType.MONASTERY,
                0,
                List.of()
        );
    }

    private static TileFeature city(
            int id,
            int shields,
            Direction... sides
    ) {
        return new TileFeature(
                id,
                FeatureType.CITY,
                shields,
                centerContacts(sides)
        );
    }

    private static TileFeature road(
            int id,
            Direction... sides
    ) {
        return new TileFeature(
                id,
                FeatureType.ROAD,
                0,
                centerContacts(sides)
        );
    }

    private static TileFeature field(
            int id,
            List<EdgeContact> contacts
    ) {
        return new TileFeature(
                id,
                FeatureType.FIELD,
                0,
                contacts
        );
    }

    private static FieldCityAdjacency adjacency(
            TileFeature field,
            TileFeature city
    ) {
        return new FieldCityAdjacency(field, city);
    }

    private static List<EdgeContact> centerContacts(
            Direction... sides
    ) {
        List<EdgeContact> result = new ArrayList<>();

        for (Direction side : sides) {
            result.add(
                    lane(side, EdgeLane.CENTER)
            );
        }

        return List.copyOf(result);
    }

    /**
     * Uma borda FIELD inteira pertence ao mesmo campo nas três lanes.
     */
    private static List<EdgeContact> fullFieldEdges(
            Direction... sides
    ) {
        List<EdgeContact> result = new ArrayList<>();

        for (Direction side : sides) {
            result.add(lane(side, EdgeLane.LEFT));
            result.add(lane(side, EdgeLane.CENTER));
            result.add(lane(side, EdgeLane.RIGHT));
        }

        return List.copyOf(result);
    }

    private static List<EdgeContact> combine(
            List<EdgeContact> base,
            EdgeContact... additional
    ) {
        List<EdgeContact> result = new ArrayList<>(base);
        result.addAll(List.of(additional));
        return List.copyOf(result);
    }

    private static EdgeContact lane(
            Direction side,
            EdgeLane lane
    ) {
        return new EdgeContact(side, lane);
    }

    private static void addCopies(
            List<Tile> tiles,
            int count,
            Supplier<Tile> supplier
    ) {
        for (int i = 0; i < count; i++) {
            tiles.add(supplier.get());
        }
    }
}
