package com.carcassonne.application.port.in;

import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.tile.Tile;

import java.util.List;
import java.util.Optional;

/** Port de entrada (hexagonal) do jogo. */
public interface GameInputPort {

    void startGame(List<PlayerSetup> players);

    Optional<Tile> drawTile();

    List<Placement> getLegalPlacements();

    void placeTile(Placement placement);

    List<FeatureRef> getLegalMeepleFeatures();

    void placeMeeple(FeatureRef feature);

    void skipMeeple();
}
