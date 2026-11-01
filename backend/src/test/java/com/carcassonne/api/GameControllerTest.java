package com.carcassonne.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Ciclo completo da API REST: iniciar, comprar, listar posições, colocar peça e pular meeple")
    void testGameRestCycle() throws Exception {
        // 1. Iniciar partida com 2 jogadores
        String startPayload = """
            {
              "players": [
                { "name": "Alice", "color": "RED" },
                { "name": "Bob", "color": "BLUE" }
              ]
            }
            """;

        mockMvc.perform(post("/api/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(startPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.phase").value("DRAW_TILE"))
                .andExpect(jsonPath("$.currentPlayerIndex").value(0))
                .andExpect(jsonPath("$.players", hasSize(2)))
                .andExpect(jsonPath("$.board", hasSize(1)))
                .andExpect(jsonPath("$.board[0].tileId").value("START"))
                .andExpect(jsonPath("$.board[0].x").value(0))
                .andExpect(jsonPath("$.board[0].y").value(0));

        // 2. Consultar estado da partida
        mockMvc.perform(get("/api/game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.phase").value("DRAW_TILE"));

        // 3. Comprar peça (draw) -> avança para PLACE_TILE com posições legais
        mockMvc.perform(post("/api/game/draw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("PLACE_TILE"))
                .andExpect(jsonPath("$.drawnTile").isMap())
                .andExpect(jsonPath("$.drawnTile.tileId").isNotEmpty())
                .andExpect(jsonPath("$.placements", not(empty())));

        // 4. Consultar posições legais no endpoint específico
        mockMvc.perform(get("/api/game/placements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // 5. Obter a primeira posição válida do estado e colocar a peça
        String stateJson = mockMvc.perform(get("/api/game"))
                .andReturn().getResponse().getContentAsString();

        // Extrai uma posição válida simples
        org.json.JSONObject obj = new org.json.JSONObject(stateJson);
        org.json.JSONArray placements = obj.getJSONArray("placements");
        org.json.JSONObject first = placements.getJSONObject(0);

        String tilePayload = String.format("""
            { "x": %d, "y": %d, "rotation": "%s" }
            """, first.getInt("x"), first.getInt("y"), first.getString("rotation"));

        mockMvc.perform(post("/api/game/tile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tilePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("PLACE_MEEPLE"))
                .andExpect(jsonPath("$.board", hasSize(2)))
                .andExpect(jsonPath("$.placedTile").isMap());

        // 6. Consultar opções de meeple no endpoint específico
        mockMvc.perform(get("/api/game/meeples"))
                .andExpect(status().isOk());

        // 7. Pular meeple -> finaliza turno e passa para o jogador Bob (DRAW_TILE)
        mockMvc.perform(post("/api/game/skip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerIndex").value(1))
                .andExpect(jsonPath("$.phase").value("DRAW_TILE"));
    }

    @Test
    @DisplayName("Requisições inválidas devem retornar erros apropriados (400 / 409)")
    void testInvalidRequests() throws Exception {
        // Sem jogadores -> 400
        mockMvc.perform(post("/api/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
