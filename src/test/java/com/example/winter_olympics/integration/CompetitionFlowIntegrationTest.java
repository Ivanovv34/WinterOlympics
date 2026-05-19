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
class CompetitionFlowIntegrationTest {

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
    void fullSlalomFlowShouldWork() throws Exception {
        Long competitionId = createSlalomCompetition();
        Long athleteOneId = createAthlete("Marco", "Odermatt", "Switzerland");
        Long athleteTwoId = createAthlete("Henrik", "Kristoffersen", "Norway");
        Long athleteThreeId = createAthlete("Alexis", "Pinturault", "France");

        registerAthlete(competitionId, athleteOneId);
        registerAthlete(competitionId, athleteTwoId);
        registerAthlete(competitionId, athleteThreeId);

        enterFirstRun(competitionId, athleteOneId, "55.321");
        enterFirstRun(competitionId, athleteTwoId, "56.900");
        enterFirstRun(competitionId, athleteThreeId, "54.800");

        mockMvc.perform(post("/api/competitions/{competitionId}/slalom/qualify-second-run", competitionId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qualificationLimit": 3
                                }
                                """))
                .andExpect(status().isOk());

        enterSecondRun(competitionId, athleteOneId, "54.210");
        enterSecondRun(competitionId, athleteTwoId, "53.900");
        enterSecondRun(competitionId, athleteThreeId, "55.100");

        mockMvc.perform(post("/api/competitions/{competitionId}/slalom/calculate-ranking", competitionId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rankPosition", is(1)))
                .andExpect(jsonPath("$[0].medal", is("GOLD")))
                .andExpect(jsonPath("$[1].rankPosition", is(2)))
                .andExpect(jsonPath("$[1].medal", is("SILVER")))
                .andExpect(jsonPath("$[2].rankPosition", is(3)))
                .andExpect(jsonPath("$[2].medal", is("BRONZE")));

        mockMvc.perform(get("/api/competitions/{competitionId}/slalom/ranking", competitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void creatingCompetitionWithoutAdminAuthShouldReturnUnauthorized() throws Exception {
        CreateCompetitionRequest request = new CreateCompetitionRequest(
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                18,
                LocalDate.of(2026, 6, 1)
        );

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fullBiathlonFlowShouldWork() throws Exception {
        Long competitionId = createBiathlonCompetition();

        Long athleteOneId = createAthlete("Johannes", "Boe", "Norway");
        Long athleteTwoId = createAthlete("Sturla", "Laegreid", "Norway");
        Long athleteThreeId = createAthlete("Quentin", "Fillon Maillet", "France");

        registerAthlete(competitionId, athleteOneId);
        registerAthlete(competitionId, athleteTwoId);
        registerAthlete(competitionId, athleteThreeId);

        enterBiathlonResult(competitionId, athleteOneId, "1500.500", 1);
        enterBiathlonResult(competitionId, athleteTwoId, "1480.250", 2);
        enterBiathlonResult(competitionId, athleteThreeId, "1520.000", 0);

        mockMvc.perform(post("/api/competitions/{competitionId}/biathlon/calculate-ranking", competitionId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rankPosition", is(1)))
                .andExpect(jsonPath("$[0].medal", is("GOLD")))
                .andExpect(jsonPath("$[0].athleteFullName", is("Quentin Fillon Maillet")))
                .andExpect(jsonPath("$[1].rankPosition", is(2)))
                .andExpect(jsonPath("$[1].medal", is("SILVER")))
                .andExpect(jsonPath("$[1].athleteFullName", is("Johannes Boe")))
                .andExpect(jsonPath("$[2].rankPosition", is(3)))
                .andExpect(jsonPath("$[2].medal", is("BRONZE")))
                .andExpect(jsonPath("$[2].athleteFullName", is("Sturla Laegreid")));

        mockMvc.perform(get("/api/competitions/{competitionId}/biathlon/ranking", competitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(jsonPath("$[0].medal", is("GOLD")))
                .andExpect(jsonPath("$[1].medal", is("SILVER")))
                .andExpect(jsonPath("$[2].medal", is("BRONZE")));
    }

    private Long createSlalomCompetition() throws Exception {
        CreateCompetitionRequest request = new CreateCompetitionRequest(
                "Men Slalom 2026",
                CompetitionType.SLALOM,
                Gender.MALE,
                18,
                LocalDate.of(2026, 6, 1)
        );

        String response = mockMvc.perform(post("/api/competitions")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Men Slalom 2026")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);

        return jsonNode.get("id").asLong();
    }

    private Long createAthlete(String firstName, String lastName, String country) throws Exception {
        CreateAthleteRequest request = new CreateAthleteRequest(
                firstName,
                lastName,
                country,
                Gender.MALE,
                LocalDate.of(1997, 10, 8)
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

    private void enterFirstRun(Long competitionId, Long athleteId, String firstRunTime) throws Exception {
        mockMvc.perform(post("/api/competitions/{competitionId}/slalom/first-run", competitionId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "athleteId": %d,
                                  "firstRunTime": %s,
                                  "didNotFinish": false
                                }
                                """.formatted(athleteId, firstRunTime)))
                .andExpect(status().isOk());
    }

    private void enterSecondRun(Long competitionId, Long athleteId, String secondRunTime) throws Exception {
        mockMvc.perform(post("/api/competitions/{competitionId}/slalom/second-run", competitionId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "athleteId": %d,
                                  "secondRunTime": %s,
                                  "didNotFinish": false
                                }
                                """.formatted(athleteId, secondRunTime)))
                .andExpect(status().isOk());
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
                .andExpect(jsonPath("$.name", is("Men Biathlon 2026")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);

        return jsonNode.get("id").asLong();
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