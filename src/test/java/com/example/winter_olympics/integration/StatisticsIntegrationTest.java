package com.example.winter_olympics.integration;

import com.example.winter_olympics.athlete.dto.CreateAthleteRequest;
import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.competition.dto.CreateCompetitionRequest;
import com.example.winter_olympics.competition.model.CompetitionType;
import com.example.winter_olympics.user.dto.RegisterRequest;
import com.example.winter_olympics.user.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
        scripts = "/sql/cleanup.sql",
        executionPhase = BEFORE_TEST_METHOD
)
class StatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUpAdminUser() throws Exception {
        RegisterRequest adminRequest = new RegisterRequest(
                "admin",
                "admin123",
                Role.ADMIN
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void statisticsEndpointsShouldReturnDataAfterBiathlonRanking() throws Exception {
        Long competitionId = createBiathlonCompetition();

        Long athleteOneId = createAthlete("Johannes", "Boe", "Norway", LocalDate.of(1993, 5, 16));
        Long athleteTwoId = createAthlete("Sturla", "Laegreid", "Norway", LocalDate.of(1997, 2, 20));
        Long athleteThreeId = createAthlete("Quentin", "Fillon Maillet", "France", LocalDate.of(1992, 8, 16));

        registerAthlete(competitionId, athleteOneId);
        registerAthlete(competitionId, athleteTwoId);
        registerAthlete(competitionId, athleteThreeId);

        enterBiathlonResult(competitionId, athleteOneId, "1500.500", 1);
        enterBiathlonResult(competitionId, athleteTwoId, "1480.250", 2);
        enterBiathlonResult(competitionId, athleteThreeId, "1520.000", 0);

        mockMvc.perform(post("/api/competitions/{competitionId}/biathlon/calculate-ranking", competitionId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/statistics/medals-by-country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/statistics/average-age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCount", is(3)));

        mockMvc.perform(get("/api/statistics/youngest-medalist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medal").exists());

        mockMvc.perform(get("/api/statistics/oldest-medalist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medal").exists());
    }

    private Long createBiathlonCompetition() throws Exception {
        CreateCompetitionRequest request = new CreateCompetitionRequest(
                "Men Biathlon 2026",
                CompetitionType.BIATHLON,
                Gender.MALE,
                18,
                LocalDate.of(2026, 6, 5)
        );

        String response = mockMvc.perform(post("/api/competitions")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);

        return jsonNode.get("id").asLong();
    }

    private Long createAthlete(
            String firstName,
            String lastName,
            String country,
            LocalDate birthDate
    ) throws Exception {
        CreateAthleteRequest request = new CreateAthleteRequest(
                firstName,
                lastName,
                country,
                Gender.MALE,
                birthDate
        );

        String response = mockMvc.perform(post("/api/athletes")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);

        return jsonNode.get("id").asLong();
    }

    private void registerAthlete(Long competitionId, Long athleteId) throws Exception {
        mockMvc.perform(post("/api/competitions/{competitionId}/registrations/{athleteId}", competitionId, athleteId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated());
    }

    private void enterBiathlonResult(
            Long competitionId,
            Long athleteId,
            String skiTime,
            int missedShots
    ) throws Exception {
        mockMvc.perform(post("/api/competitions/{competitionId}/biathlon/results", competitionId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "athleteId": %d,
                                  "skiTime": %s,
                                  "missedShots": %d,
                                  "didNotFinish": false
                                }
                                """.formatted(athleteId, skiTime, missedShots)))
                .andExpect(status().isOk());
    }
}