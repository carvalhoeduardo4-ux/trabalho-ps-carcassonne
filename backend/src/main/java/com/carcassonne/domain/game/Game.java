package com.carcassonne.domain.game;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.exception.InvalidMeeplePlacementException;
import com.carcassonne.domain.exception.InvalidPlacementException;
import com.carcassonne.domain.exception.InvalidTurnPhaseException;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.rules.MeeplePlacementRules;
import com.carcassonne.domain.rules.ScoreAward;
import com.carcassonne.domain.rules.TilePlacementRules;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.StandardTiles;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root / GRASP Information Expert.
 */
@Getter
public class Game {

    public static final int INITIAL_MEEPLES_PER_PLAYER = 7;

    private final Board board;
    private final TileDeck deck;
    private final List<Player> players;
    private final Turn turn;
    private final List<MeeplePlacement> meeples = new ArrayList<>();

    private final TilePlacementRules tileRules;
    private final MeeplePlacementRules meepleRules;

    private int currentPlayerIndex;
    private GameStatus status = GameStatus.ACTIVE;

    /**
     * Cria e inicializa uma nova partida utilizando as implementações
     * padrão das regras.
     *
     * Mantido principalmente por compatibilidade com testes e outros
     * pontos que ainda constroem Game diretamente.
     */
    public Game(List<Player> players) {
        this(
                players,
                new TilePlacementRules(),
                new MeeplePlacementRules()
        );
    }

    /**
     * Cria e inicializa uma nova partida conforme o diagrama:
     *
     * 1. Valida os jogadores;
     * 2. Armazena as regras utilizadas pelo agregado;
     * 3. Cria o tabuleiro;
     * 4. Cria e embaralha o monte padrão;
     * 5. Cria o turno inicial;
     * 6. Posiciona a peça inicial em (0,0);
     * 7. Define a partida como ativa.
     */
    public Game(
            List<Player> players,
            TilePlacementRules tileRules,
            MeeplePlacementRules meepleRules
    ) {
        validatePlayers(players);

        this.tileRules = Objects.requireNonNull(
                tileRules,
                "TilePlacementRules não pode ser nulo."
        );

        this.meepleRules = Objects.requireNonNull(
                meepleRules,
                "MeeplePlacementRules não pode ser nulo."
        );

        this.players = new ArrayList<>(players);

        this.board = new Board();

        this.deck = new TileDeck(
                StandardTiles.createStandardDrawTiles()
        );
        this.deck.shuffle();

        this.turn = new Turn();

        PlacedTile startTile = new PlacedTile(
                StandardTiles.createStarterTile(),
                new Position(0, 0),
                Rotation.DEG_0
        );

        this.board.place(startTile);

        this.status = GameStatus.ACTIVE;
        this.currentPlayerIndex = 0;
    }

    /**
     * Construtor flexível com componentes injetados.
     *
     * Mantido para testes ou eventual carregamento de uma partida.
     */
    public Game(
            Board board,
            TileDeck deck,
            List<Player> players,
            Turn turn
    ) {
        this(
                board,
                deck,
                players,
                turn,
                new TilePlacementRules(),
                new MeeplePlacementRules()
        );
    }

    /**
     * Variante do construtor flexível que também recebe as regras
     * utilizadas pelo agregado.
     */
    public Game(
            Board board,
            TileDeck deck,
            List<Player> players,
            Turn turn,
            TilePlacementRules tileRules,
            MeeplePlacementRules meepleRules
    ) {
        validatePlayers(players);

        this.tileRules = Objects.requireNonNull(
                tileRules,
                "TilePlacementRules não pode ser nulo."
        );

        this.meepleRules = Objects.requireNonNull(
                meepleRules,
                "MeeplePlacementRules não pode ser nulo."
        );

        this.players = new ArrayList<>(players);

        this.board = board != null
                ? board
                : new Board();

        this.deck = deck != null
                ? deck
                : new TileDeck(StandardTiles.createStandardDrawTiles());

        this.turn = turn != null
                ? turn
                : new Turn();

        this.status = GameStatus.ACTIVE;
        this.currentPlayerIndex = 0;
    }

    private void validatePlayers(List<Player> players) {
        if (players == null || players.size() < 2 || players.size() > 5) {
            throw new IllegalArgumentException(
                    "A partida deve ter entre 2 e 5 jogadores."
            );
        }

        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException(
                        "Jogador não pode ser nulo."
                );
            }
        }

        long distinctColors = players.stream()
                .map(Player::getColor)
                .distinct()
                .count();

        if (distinctColors != players.size()) {
            throw new IllegalArgumentException(
                    "Cada jogador deve possuir uma cor distinta."
            );
        }

        for (Player player : players) {
            if (availableMeeples(player) != INITIAL_MEEPLES_PER_PLAYER) {
                throw new IllegalStateException(
                        "Cada jogador deve iniciar com "
                                + INITIAL_MEEPLES_PER_PLAYER
                                + " meeples disponíveis."
                );
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
        return turn != null
                ? turn.drawnTile()
                : Optional.empty();
    }

    public Optional<PlacedTile> currentPlacedTile() {
        return turn != null
                ? turn.placedTile()
                : Optional.empty();
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
            throw new InvalidTurnPhaseException(
                    "A partida já foi encerrada."
            );
        }
    }

    // =====================================================
    // DRAW TILE
    // =====================================================

    public Tile drawTile() {
        requireActive();
        turn.requireDrawTile();

        Tile tile = deck.draw();

        turn.recordDrawnTile(tile);

        return tile;
    }

    /**
     * Retorna as colocações legais para a peça sorteada atual.
     *
     * A regra é delegada para TilePlacementRules, mas o estado
     * interno do agregado não é exposto ao chamador.
     */
    public List<Placement> legalPlacements() {
        requireActive();
        turn.requirePlaceTile();

        Tile tile = currentDrawnTile()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Nenhuma peça sorteada."
                        )
                );

        return tileRules.legalPlacements(
                board,
                tile
        );
    }

    /**
     * Uma peça só pode ser descartada se realmente não existir
     * nenhuma colocação legal.
     */
    public void discardDrawnTile() {
        requireActive();
        turn.requirePlaceTile();

        Tile tile = currentDrawnTile()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Nenhuma peça sorteada."
                        )
                );

        if (!tileRules.legalPlacements(board, tile).isEmpty()) {
            throw new InvalidPlacementException(
                    "A peça não pode ser descartada enquanto houver colocações legais."
            );
        }

        turn.discardDrawnTile();
    }

    // =====================================================
    // PLACE TILE
    // =====================================================

    /**
     * Valida e posiciona a peça sorteada.
     *
     * Game protege a invariante do agregado e delega apenas
     * a regra específica de colocação para TilePlacementRules.
     */
    public PlacedTile placeTile(Placement placement) {
        requireActive();
        turn.requirePlaceTile();

        Tile tile = currentDrawnTile()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Nenhuma peça sorteada."
                        )
                );

        if (!tileRules.canPlace(board, tile, placement)) {
            throw new InvalidPlacementException(
                    "Colocação de peça inválida: " + placement
            );
        }

        PlacedTile placed = new PlacedTile(
                tile,
                placement.getPosition(),
                placement.getRotation()
        );

        board.place(placed);

        turn.recordPlacedTile(placed);

        return placed;
    }

    // =====================================================
    // MEEPLE
    // =====================================================

    /**
     * Retorna as features da peça recém-colocada nas quais
     * o jogador atual pode colocar um meeple.
     */
    public List<FeatureRef> legalMeepleFeatures() {
        requireActive();
        turn.requireMeepleDecision();

        Player player = currentPlayer();

        if (availableMeeples(player) <= 0) {
            return List.of();
        }

        PlacedTile placed = currentPlacedTile()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Nenhuma peça posicionada."
                        )
                );

        return meepleRules.legalFeatures(
                board,
                meeples,
                placed
        );
    }

    /**
     * Coloca um meeple na feature indicada.
     *
     * Game verifica as invariantes que dependem do estado do agregado:
     * - fase correta;
     * - feature pertencente à peça deste turno;
     * - jogador com meeple disponível.
     *
     * MeeplePlacementRules verifica a legalidade da região conectada.
     */
    public void placeMeeple(FeatureRef feature) {
        requireActive();
        turn.requireMeepleDecision();

        PlacedTile placed = currentPlacedTile()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Nenhuma peça posicionada."
                        )
                );

        if (feature == null
                || !placed.getPosition().equals(feature.getPosition())) {

            throw new InvalidMeeplePlacementException(
                    "O meeple deve ficar na peça colocada neste turno."
            );
        }

        Player player = currentPlayer();

        if (availableMeeples(player) <= 0) {
            throw new InvalidMeeplePlacementException(
                    "O jogador não possui meeples disponíveis."
            );
        }

        if (!meepleRules.canPlace(
                board,
                meeples,
                feature
        )) {
            throw new InvalidMeeplePlacementException(
                    "Colocação de meeple inválida: " + feature
            );
        }

        meeples.add(
                new MeeplePlacement(
                        player,
                        feature
                )
        );

        turn.resolveMeeple();
    }

    public void skipMeeple() {
        requireActive();
        turn.requireMeepleDecision();

        turn.resolveMeeple();
    }

    // =====================================================
    // SCORING
    // =====================================================

    /*
     * Mantido como placeholder neste PR.
     *
     * A delegação de ScoringRules será feita no PR2.
     */
    public void scoreEndGame() {
        throw new UnsupportedOperationException(
                "Not implemented"
        );
    }

    /**
     * Soma os pontos aos recebedores e devolve os meeples
     * das regiões pontuadas.
     *
     * Continua público temporariamente neste PR.
     * Será tornado privado no PR2.
     */
    public void applyAwards(List<ScoreAward> awards) {
        for (ScoreAward award : awards) {
            award.getRecipients()
                    .forEach(
                            player -> player.addScore(
                                    award.getPoints()
                            )
                    );

            removeMeeples(
                    award.getRegion()
            );
        }
    }

    private void removeMeeples(FeatureRegion region) {
        meeples.removeIf(
                meeple -> region.contains(
                        meeple.getFeature()
                )
        );
    }

    // =====================================================
    // TURN / GAME LIFECYCLE
    // =====================================================

    public boolean hasTilesRemaining() {
        return deck != null && !deck.isEmpty();
    }

    public void nextPlayer() {
        currentPlayerIndex =
                (currentPlayerIndex + 1) % players.size();

        turn.reset();
    }

    public void finish() {
        this.status = GameStatus.FINISHED;
    }

    /**
     * Retorna todos os jogadores empatados com a maior pontuação.
     *
     * Nenhum estado adicional de vencedor é armazenado.
     */
    public List<Player> winners() {
        int highestScore = players.stream()
                .mapToInt(Player::getScore)
                .max()
                .orElseThrow();

        return players.stream()
                .filter(
                        player ->
                                player.getScore() == highestScore
                )
                .toList();
    }

    // =====================================================
    // READ-ONLY ACCESS
    // =====================================================

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
        return Collections.unmodifiableList(
                players
        );
    }

    public List<MeeplePlacement> getMeeples() {
        return Collections.unmodifiableList(
                meeples
        );
    }
}