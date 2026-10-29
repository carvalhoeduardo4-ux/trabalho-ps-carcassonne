package com.carcassonne.domain.game;

import com.carcassonne.domain.board.Board;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.FeatureRegion;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.player.MeeplePlacement;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileDeck;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Aggregate root / GRASP Information Expert. */
@RequiredArgsConstructor
public class Game {

    private final Board board;
    private final TileDeck deck;
    private final List<Player> players;      // 2..5
    private final Turn turn;
    private final List<MeeplePlacement> meeples = new ArrayList<>();

    private int currentPlayerIndex;
    private GameStatus status = GameStatus.ACTIVE;

    public Player currentPlayer() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<Tile> currentDrawnTile() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Optional<PlacedTile> currentPlacedTile() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int availableMeeples(Player player) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Tile drawTile() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void discardDrawnTile() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public PlacedTile placeTile(Placement placement) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void placeMeeple(FeatureRef feature) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void skipMeeple() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void applyAwards(List<ScoreAward> awards) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void removeMeeples(FeatureRegion region) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean hasTilesRemaining() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void nextPlayer() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void finish() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Player> winners() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
