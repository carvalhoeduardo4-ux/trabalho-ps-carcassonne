package com.carcassonne.application.service;

import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.game.DrawTileState;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.game.GameStatus;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.tile.Rotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameInitializationTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
    }

    @Test
    @DisplayName("Inicialização com 2 jogadores válidos deve configurar partida ativa, tabuleiro e monte")
    void testStartGameSuccessWithTwoPlayers() {
        List<PlayerSetup> setups = List.of(
            new PlayerSetup("Alice", PlayerColor.RED),
            new PlayerSetup("Bob", PlayerColor.BLUE)
        );

        engine.startGame(setups);
        Game game = engine.getGame();

        assertNotNull(game, "A instância de Game deve ser criada");
        assertEquals(GameStatus.ACTIVE, game.getStatus(), "O status inicial deve ser ACTIVE");
        assertEquals(0, game.getCurrentPlayerIndex(), "O índice do primeiro jogador deve ser 0");
        assertNotNull(game.currentPlayer(), "Deve haver jogador atual");
        assertEquals("Alice", game.currentPlayer().getName());
        assertEquals(PlayerColor.RED, game.currentPlayer().getColor());

        // Invariante: 7 meeples disponíveis por jogador
        for (Player player : game.getPlayers()) {
            assertEquals(7, game.availableMeeples(player),
                    "Cada jogador deve iniciar com 7 meeples disponíveis");
        }

        // Tabuleiro inicializado com a peça inicial em (0, 0) DEG_0
        Board board = game.getBoard();
        assertNotNull(board, "O tabuleiro deve ser criado");
        assertTrue(board.isOccupied(new Position(0, 0)), "A posição (0,0) deve estar ocupada pela peça inicial");
        assertEquals(1, board.getTiles().size(), "O tabuleiro deve conter exatamente 1 peça inicial");

        PlacedTile startTile = board.tileAt(new Position(0, 0)).orElseThrow();
        assertEquals(new Position(0, 0), startTile.getPosition());
        assertEquals(Rotation.DEG_0, startTile.getRotation());

        // Monte com 71 peças padrão de compra (peça inicial não pertence ao monte)
        assertNotNull(game.getDeck(), "O monte deve ser criado");
        assertEquals(71, game.getDeck().remaining(),
                "O monte deve conter exatamente as 71 peças de compra do jogo-base");
        assertFalse(game.getDeck().isEmpty());
        assertTrue(game.hasTilesRemaining());

        // Turno inicial com fase DRAW_TILE (DrawTileState)
        assertNotNull(game.getTurn(), "O turno deve ser criado");
        assertInstanceOf(DrawTileState.class, game.getTurn().getState(),
                "A fase inicial do turno deve ser DRAW_TILE");
        assertTrue(game.currentDrawnTile().isEmpty());
        assertTrue(game.currentPlacedTile().isEmpty());
    }

    @Test
    @DisplayName("Suporta 2 a 5 jogadores com cores distintas")
    void testSupportedPlayerCounts() {
        for (int count = 2; count <= 5; count++) {
            List<PlayerSetup> setups = createSetups(count);
            GameEngine currentEngine = new GameEngine();
            assertDoesNotThrow(() -> currentEngine.startGame(setups));
            assertEquals(count, currentEngine.getGame().getPlayers().size());
        }
    }

    @Test
    @DisplayName("Deve rejeitar partida com menos de 2 jogadores")
    void testRejectLessThanTwoPlayers() {
        List<PlayerSetup> setups = List.of(new PlayerSetup("Solo", PlayerColor.RED));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> engine.startGame(setups));
        assertTrue(ex.getMessage().contains("2 e 5"));
    }

    @Test
    @DisplayName("Deve rejeitar partida com mais de 5 jogadores")
    void testRejectMoreThanFivePlayers() {
        List<PlayerSetup> setups = List.of(
            new PlayerSetup("P1", PlayerColor.RED),
            new PlayerSetup("P2", PlayerColor.BLUE),
            new PlayerSetup("P3", PlayerColor.GREEN),
            new PlayerSetup("P4", PlayerColor.YELLOW),
            new PlayerSetup("P5", PlayerColor.BLACK),
            new PlayerSetup("P6", PlayerColor.RED)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> engine.startGame(setups));
        assertTrue(ex.getMessage().contains("2 e 5"));
    }

    @Test
    @DisplayName("Deve rejeitar jogadores com cores repetidas")
    void testRejectDuplicateColors() {
        List<PlayerSetup> setups = List.of(
            new PlayerSetup("Alice", PlayerColor.RED),
            new PlayerSetup("Bob", PlayerColor.RED)
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> engine.startGame(setups));
        assertTrue(ex.getMessage().contains("cor distinta"));
    }

    @Test
    @DisplayName("Deve rejeitar lista nula de jogadores")
    void testRejectNullPlayerList() {
        assertThrows(IllegalArgumentException.class, () -> engine.startGame(null));
    }

    private List<PlayerSetup> createSetups(int count) {
        PlayerColor[] colors = PlayerColor.values();
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new PlayerSetup("Player " + (i + 1), colors[i]))
                .toList();
    }
}
