package com.carcassonne.api.dto;

import java.util.List;

/** DTOs da API REST (contrato com o frontend). */
public final class GameDtos {

    private GameDtos() {}

    public record PlayerRequest(String name, String color) {}

    public record StartGameRequest(List<PlayerRequest> players) {}

    public record PositionDto(int x, int y) {}

    public record PlacementRequest(int x, int y, String rotation) {}

    public record MeepleRequest(int x, int y, int featureId) {}

    public record EdgeContactDto(String side, String lane) {}

    public record FeatureDto(int id, String type, int shieldCount, List<EdgeContactDto> edgeContacts) {}

    public record TileDto(String tileId, List<FeatureDto> features) {}

    public record PlacedTileDto(String tileId, int x, int y, String rotation, List<FeatureDto> features) {}

    public record PlacementDto(int x, int y, String rotation) {}

    public record FeatureRefDto(int x, int y, int featureId) {}

    public record MeepleDto(int playerIndex, String color, int x, int y, int featureId) {}

    public record PlayerDto(String name, String color, int score, int availableMeeples) {}

    public record GameStateDto(
            String status,
            String phase,
            int currentPlayerIndex,
            List<PlayerDto> players,
            int deckRemaining,
            TileDto drawnTile,
            PlacedTileDto placedTile,
            List<PlacedTileDto> board,
            List<MeepleDto> meeples,
            List<PlacementDto> placements,
            List<FeatureRefDto> meepleOptions) {}

    public record ErrorDto(String error, String message) {}
}
