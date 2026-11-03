package com.carcassonne.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private void start(String playersJson) throws Exception {
        mockMvc.perform(post("/api/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"players\": " + playersJson + " }"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Jogador sem tipo é humano")
    void typeDefaultsToHuman() throws Exception {
        mockMvc.perform(post("/api/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "players": [
                                { "name": "Alice", "color": "RED" },
                                { "name": "Bot", "color": "BLUE", "type": "AI" }
                            ] }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].type").value("HUMAN"))
                .andExpect(jsonPath("$.players[1].type").value("AI"));
    }

    @Test
    @DisplayName("Tipo de jogador inválido retorna 400")
    void invalidTypeIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "players": [
                                { "name": "Alice", "color": "RED", "type": "ROBOT" },
                                { "name": "Bob", "color": "BLUE" }
                            ] }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Pedir decisão na vez de um humano retorna 409")
    void humanTurnIs409() throws Exception {
        start("""
            [ { "name": "Alice", "color": "RED", "type": "HUMAN" },
              { "name": "Bot", "color": "BLUE", "type": "AI" } ]
            """);

        mockMvc.perform(get("/api/game/ai/decision"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NotAiPlayerTurnException"));
    }

    @Test
    @DisplayName("Ciclo de um turno da IA: decidir, aplicar peça, decidir e aplicar meeple")
    void aiTurnDecisionCycle() throws Exception {
        start("""
            [ { "name": "Bot1", "color": "RED", "type": "AI" },
              { "name": "Bot2", "color": "BLUE", "type": "AI" } ]
            """);

        mockMvc.perform(get("/api/game/ai/decision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("DRAW_TILE"))
                .andExpect(jsonPath("$.placement").value(nullValue()))
                .andExpect(jsonPath("$.feature").value(nullValue()));

        mockMvc.perform(post("/api/game/draw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("PLACE_TILE"));

        JSONObject tileDecision = new JSONObject(
                mockMvc.perform(get("/api/game/ai/decision"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.action").value("PLACE_TILE"))
                        .andReturn().getResponse().getContentAsString()
        );

        JSONObject placement = tileDecision.getJSONObject("placement");
        int x = placement.getInt("x");
        int y = placement.getInt("y");
        String rotation = placement.getString("rotation");

        JSONArray placements = new JSONObject(
                mockMvc.perform(get("/api/game")).andReturn().getResponse().getContentAsString()
        ).getJSONArray("placements");

        boolean listed = false;
        for (int i = 0; i < placements.length(); i++) {
            JSONObject p = placements.getJSONObject(i);
            if (p.getInt("x") == x && p.getInt("y") == y && p.getString("rotation").equals(rotation)) {
                listed = true;
            }
        }
        assertTrue(listed, "A decisão deve estar entre as colocações legais.");

        mockMvc.perform(post("/api/game/tile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{ \"x\": %d, \"y\": %d, \"rotation\": \"%s\" }", x, y, rotation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("PLACE_MEEPLE"));

        JSONObject meepleDecision = new JSONObject(
                mockMvc.perform(get("/api/game/ai/decision"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.action", anyOf(is("PLACE_MEEPLE"), is("SKIP_MEEPLE"))))
                        .andReturn().getResponse().getContentAsString()
        );

        if ("PLACE_MEEPLE".equals(meepleDecision.getString("action"))) {
            mockMvc.perform(post("/api/game/meeple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(meepleDecision.getJSONObject("feature").toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPlayerIndex").value(1));
        } else {
            mockMvc.perform(post("/api/game/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPlayerIndex").value(1));
        }
    }
}
