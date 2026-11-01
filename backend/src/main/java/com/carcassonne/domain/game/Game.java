package com.carcassonne.domain.game;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.exception.InvalidMeeplePlacementException;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.rules.ScoreAward;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Aggregate root / GRASP Information Expert. */
@Getter
public class Game {

    public static final int INITIAL_MEEPLES_PER_PLAYER = 7;

    private final Board board;
    private final TileDeck deck;
    private final List<Player> players;      // 2..5
    private final Turn turn;
    private final List<MeeplePlacement> meeples = new ArrayList<>();

    private int currentPlayerIndex;
    private GameStatus status = GameStatus.ACTIVE;

    /**
     * Cria e inicializa uma nova partida conforme o diagrama de inicialização:
     * 1. Valida invariantes (2 a 5 jogadores, cores distintas, 7 meeples por jogador);
     * 2. Cria tabuleiro vazio;
     * 3. Cria monte com as peças padrão de compra (71 peças) e embaralha;
     * 4. Cria turno inicial (fase DrawTile);
     * 5. Cria e posiciona a peça inicial no centro em (0, 0) com rotação DEG_0;
     * 6. Define estado ativo e primeiro jogador (índice 0).
     */
    public Game(List<Player> players) {
        validatePlayers(players);
        this.players = new ArrayList<>(players);
        this.board = new Board();
        this.deck = new TileDeck(StandardTiles.createStandardDrawTiles());
        this.deck.shuffle();
        this.turn = new Turn();

        PlacedTile startTile = new PlacedTile(StandardTiles.createStarterTile(), new Position(0, 0), Rotation.DEG_0);
        this.board.place(startTile);

        this.status = GameStatus.ACTIVE;
        this.currentPlayerIndex = 0;
    }

    /**
     * Construtor flexível com componentes injetados (para suporte a testes ou carregamento).
     */
    public Game(Board board, TileDeck deck, List<Player> players, Turn turn) {
        validatePlayers(players);
        this.players = new ArrayList<>(players);
        this.board = board != null ? board : new Board();
        this.deck = deck != null ? deck : new TileDeck(StandardTiles.createStandardDrawTiles());
        this.turn = turn != null ? turn : new Turn();
        this.status = GameStatus.ACTIVE;
        this.currentPlayerIndex = 0;
    }

    private void validatePlayers(List<Player> players) {
        if (players == null || players.size() < 2 || players.size() > 5) {
            throw new IllegalArgumentException("A partida deve ter entre 2 e 5 jogadores.");
        }
        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("Jogador não pode ser nulo.");
            }
        }
        long distinctColors = players.stream()
                .map(Player::getColor)
                .distinct()
                .count();
        if (distinctColors != players.size()) {
            throw new IllegalArgumentException("Cada jogador deve possuir uma cor distinta.");
        }
        for (Player player : players) {
            if (availableMeeples(player) != INITIAL_MEEPLES_PER_PLAYER) {
                throw new IllegalStateException("Cada jogador deve iniciar com " + INITIAL_MEEPLES_PER_PLAYER + " meeples disponíveis.");
            }
        }
    }

    public Player currentPlayer() {
        if (players == null || players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public Optional<Tile> currentDrawnTile() {
        return turn != null ? turn.drawnTile() : Optional.empty();
    }

    public Optional<PlacedTile> currentPlacedTile() {
        return turn != null ? turn.placedTile() : Optional.empty();
    }

    public int availableMeeples(Player player) {
        if (player == null) {
            return 0;
        }
        long placed = meeples.stream()
                .filter(m -> m.getPlayer().equals(player))
                .count();
        return INITIAL_MEEPLES_PER_PLAYER - (int) placed;
    }

    private void requireActive() {
        if (status != GameStatus.ACTIVE) {
            throw new InvalidTurnPhaseException("A partida já foi encerrada.");
        }
    }

    public Tile drawTile() {
        requireActive();
        turn.requireDrawTile();
        Tile tile = deck.draw();
        turn.recordDrawnTile(tile);
        return tile;
    }

    public void discardDrawnTile() {
        requireActive();
        turn.discardDrawnTile();
    }

    public PlacedTile placeTile(Placement placement) {
        requireActive();
        turn.requirePlaceTile();
        Tile tile = currentDrawnTile()
                .orElseThrow(() -> new IllegalStateException("Nenhuma peça sorteada."));
        PlacedTile placed = new PlacedTile(tile, placement.getPosition(), placement.getRotation());
        board.place(placed);
        turn.recordPlacedTile(placed);
        return placed;
    }

    public void placeMeeple(FeatureRef feature) {
        requireActive();
        turn.requireMeepleDecision();
        PlacedTile placed = currentPlacedTile()
                .orElseThrow(() -> new IllegalStateException("Nenhuma peça posicionada."));
        if (!placed.getPosition().equals(feature.getPosition())) {
            throw new InvalidMeeplePlacementException("O meeple deve ficar na peça colocada neste turno.");
        }
        Player player = currentPlayer();
        if (availableMeeples(player) <= 0) {
            throw new InvalidMeeplePlacementException("O jogador não possui meeples disponíveis.");
        }
        meeples.add(new MeeplePlacement(player, feature));
        turn.resolveMeeple();
    }

    public void skipMeeple() {
        requireActive();
        turn.requireMeepleDecision();
        turn.resolveMeeple();
    }

    public void scoreEndGame() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Soma os pontos aos recebedores e devolve os meeples das regiões pontuadas. */
    public void applyAwards(List<ScoreAward> awards) {
        for (ScoreAward award : awards) {
            award.getRecipients().forEach(p -> p.addScore(award.getPoints()));
            removeMeeples(award.getRegion());
        }
    }

    private void removeMeeples(FeatureRegion region) {
        meeples.removeIf(m -> region.contains(m.getFeature()));
    }

    public boolean hasTilesRemaining() {
        return deck != null && !deck.isEmpty();
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turn.reset();
    }

    public void finish() {
        this.status = GameStatus.FINISHED;
    }

    public List<Player> winners() {
    int highestScore = players.stream()
            .mapToInt(Player::getScore)
            .max()
            .orElseThrow();

    return players.stream()
            .filter(player -> player.getScore() == highestScore)
            .toList();
    }

    public Board getBoard() {
        return board;
    }

    public TileDeck getDeck() {
        return deck;
    }

    public Turn getTurn() {
        return turn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<MeeplePlacement> getMeeples() {
        return Collections.unmodifiableList(meeples);
    }
}
