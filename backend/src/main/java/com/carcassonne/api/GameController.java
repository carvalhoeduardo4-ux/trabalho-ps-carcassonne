package com.carcassonne.api;

import com.carcassonne.api.dto.GameDtos.*;
import com.carcassonne.application.port.in.PlayerSetup;
import com.carcassonne.application.service.GameEngine;
import com.carcassonne.domain.board.FeatureRef;
import com.carcassonne.domain.board.PlacedTile;
import com.carcassonne.domain.board.Placement;
import com.carcassonne.domain.board.Position;
import com.carcassonne.domain.game.Game;
import com.carcassonne.domain.player.Player;
import com.carcassonne.domain.player.PlayerColor;
import com.carcassonne.domain.tile.Rotation;
import com.carcassonne.domain.tile.Tile;
import com.carcassonne.domain.tile.TileFeature;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/** Adaptador REST (porta de entrada HTTP) sobre o {@link GameEngine}. */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameEngine engine;

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    @PostMapping
    public GameStateDto start(@RequestBody StartGameRequest request) {
        if (request == null || request.players() == null) {
            throw new IllegalArgumentException("Lista de jogadores obrigatória.");
        }
        List<PlayerSetup> setups = new ArrayList<>();
        for (PlayerRequest p : request.players()) {
            setups.add(new PlayerSetup(p.name(), PlayerColor.valueOf(p.color().toUpperCase())));
        }
        engine.startGame(setups);
        return state();
    }

    @GetMapping
    public GameStateDto state() {
        Game g = engine.getGame();
        if (g == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }
        String phase = phaseOf(g);
        List<PlacementDto> placements = List.of();
        List<FeatureRefDto> options = List.of();
        if ("PLACE_TILE".equals(phase)) {
            placements = engine.getLegalPlacements().stream()
                    .map(p -> new PlacementDto(p.getPosition().getX(), p.getPosition().getY(), p.getRotation().name()))
                    .toList();
        } else if ("PLACE_MEEPLE".equals(phase)) {
            options = engine.getLegalMeepleFeatures().stream()
                    .map(f -> new FeatureRefDto(f.getPosition().getX(), f.getPosition().getY(), f.getFeatureId()))
                    .toList();
        }
        List<Player> players = g.getPlayers();
        return new GameStateDto(
                g.getStatus().name(),
                phase,
                g.getCurrentPlayerIndex(),
                players.stream().map(p -> new PlayerDto(p.getName(), p.getColor().name(), p.getScore(),
                        g.availableMeeples(p))).toList(),
                g.getDeck().remaining(),
                g.currentDrawnTile().map(this::tileDto).orElse(null),
                g.currentPlacedTile().map(this::placedDto).orElse(null),
                g.getBoard().getTiles().stream().map(this::placedDto).toList(),
                g.getMeeples().stream().map(m -> new MeepleDto(
                        players.indexOf(m.getPlayer()), m.getPlayer().getColor().name(),
                        m.getFeature().getPosition().getX(), m.getFeature().getPosition().getY(),
                        m.getFeature().getFeatureId())).toList(),
                placements,
                options);
    }

    @GetMapping("/placements")
    public List<PlacementDto> placements() {
        Game g = engine.getGame();
        if (g == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }
        g.getTurn().requirePlaceTile();
        return engine.getLegalPlacements().stream()
                .map(p -> new PlacementDto(p.getPosition().getX(), p.getPosition().getY(), p.getRotation().name()))
                .toList();
    }

    @GetMapping("/meeples")
    public List<FeatureRefDto> meepleOptions() {
        Game g = engine.getGame();
        if (g == null) {
            throw new IllegalStateException("Nenhuma partida iniciada.");
        }
        g.getTurn().requireMeepleDecision();
        return engine.getLegalMeepleFeatures().stream()
                .map(f -> new FeatureRefDto(f.getPosition().getX(), f.getPosition().getY(), f.getFeatureId()))
                .toList();
    }

    @PostMapping("/draw")
    public GameStateDto draw() {
        engine.drawTile();
        return state();
    }

    @PostMapping("/tile")
    public GameStateDto placeTile(@RequestBody PlacementRequest request) {
        engine.placeTile(new Placement(new Position(request.x(), request.y()),
                Rotation.valueOf(request.rotation().toUpperCase())));
        return state();
    }

    @PostMapping("/meeple")
    public GameStateDto placeMeeple(@RequestBody MeepleRequest request) {
        engine.placeMeeple(new FeatureRef(new Position(request.x(), request.y()), request.featureId()));
        return state();
    }

    @PostMapping("/skip")
    public GameStateDto skipMeeple() {
        engine.skipMeeple();
        return state();
    }

    private String phaseOf(Game g) {
        if (g.getStatus() != com.carcassonne.domain.game.GameStatus.ACTIVE) {
            return "GAME_OVER";
        }
        String name = g.getTurn().getState().getClass().getSimpleName();
        return switch (name) {
            case "DrawTileState" -> "DRAW_TILE";
            case "PlaceTileState" -> "PLACE_TILE";
            case "PlaceMeepleState" -> "PLACE_MEEPLE";
            default -> name;
        };
    }

    private TileDto tileDto(Tile tile) {
        return new TileDto(tile.getId(), tile.getFeatures().stream().map(this::featureDto).toList());
    }

    private PlacedTileDto placedDto(PlacedTile p) {
        return new PlacedTileDto(p.getTile().getId(), p.getPosition().getX(), p.getPosition().getY(),
                p.getRotation().name(), p.getTile().getFeatures().stream().map(this::featureDto).toList());
    }

    private FeatureDto featureDto(TileFeature f) {
        return new FeatureDto(f.getId(), f.getType().name(), f.getShieldCount(),
                f.getEdgeContacts().stream()
                        .map(c -> new EdgeContactDto(c.getSide().name(), c.getLane().name())).toList());
    }
}
