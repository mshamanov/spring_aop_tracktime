package com.mash.aoptracktime.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mash.aoptracktime.entity.TrackTimeMethodStatus;
import com.mash.aoptracktime.entity.TrackTimeStat;
import com.mash.aoptracktime.rest.mapper.LongStatisticsToTrackTimeSummary;
import com.mash.aoptracktime.rest.mapper.TrackTimeDtoToEntityMapper;
import com.mash.aoptracktime.rest.mapper.TrackTimeDtoToSpecificationMapper;
import com.mash.aoptracktime.rest.mapper.TrackTimeEntityToDtoMapper;
import com.mash.aoptracktime.rest.model.TrackTimeDto;
import com.mash.aoptracktime.service.TrackTimeStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrackTimeRestController.class)
@ActiveProfiles("test")
class TrackTimeRestControllerMvcTest {
    @MockBean
    TrackTimeStatsService timeStatsService;

    @SpyBean
    TrackTimeDtoToEntityMapper trackTimeDtoToEntityMapper;

    @SpyBean
    TrackTimeDtoToSpecificationMapper toSpecificationMapper;

    @SpyBean
    TrackTimeEntityToDtoMapper toDtoMapper;

    @SpyBean
    LongStatisticsToTrackTimeSummary statisticsToSummaryMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    List<TrackTimeStat> trackTimeStats;

    @BeforeEach
    void setUp() {
        this.trackTimeStats = List.of(
                TrackTimeStat.builder()
                        .id(1L)
                        .groupName("async")
                        .packageName("com.mash.aoptracktime.service")
                        .methodName("addStudent")
                        .executionTime(5023L)
                        .parameters("Object")
                        .status(TrackTimeMethodStatus.COMPLETED)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                TrackTimeStat.builder()
                        .id(1L)
                        .groupName("sync")
                        .packageName("com.mash.aoptracktime.service")
                        .methodName("addEmployee")
                        .executionTime(2015L)
                        .parameters("Object")
                        .status(TrackTimeMethodStatus.EXCEPTION)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .build(),
                TrackTimeStat.builder()
                        .id(1L)
                        .groupName("sync")
                        .packageName("com.mash.aoptracktime.service")
                        .methodName("getEmployees")
                        .executionTime(1033L)
                        .parameters(null)
                        .status(TrackTimeMethodStatus.COMPLETED)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .build()
        );
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all :: query params are default")
    void handleGetStats_whenQueryParamsAreDefault_returnsAllDataWithSummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream().map(this.toDtoMapper).toList();
        var statistics = dtoList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();

        Map<String, Object> resultBody = Map.of(
                "result", dtoList,
                "summary", this.statisticsToSummaryMapper.apply(statistics)
        );
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=all")
    void handleGetStats_whenViewTypeIsAll_returnsAllDataWithSummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream().map(this.toDtoMapper).toList();
        var statistics = dtoList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();

        Map<String, Object> resultBody = Map.of(
                "result", dtoList,
                "summary", this.statisticsToSummaryMapper.apply(statistics)
        );
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "all"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=data")
    void handleGetStats_whenViewTypeIsData_returnsOnlyDataWithNoSummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream().map(this.toDtoMapper).toList();

        Map<String, Object> resultBody = Map.of("result", dtoList);
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "data"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=summary")
    void handleGetStats_whenViewTypeIsSummary_returnsOnlySummaryWithNoData() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream().map(this.toDtoMapper).toList();
        var statistics = dtoList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();

        Map<String, Object> resultBody = Map.of("summary", this.statisticsToSummaryMapper.apply(statistics));
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "summary"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=all&short=true")
    void handleGetStats_whenViewTypeIsAll_shortInfoIsTrue_returnsAllDataInShortFormatWithSummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream()
                .map(stat -> TrackTimeDto.builder()
                        .methodName(stat.getMethodName())
                        .executionTime(stat.getExecutionTime())
                        .build())
                .toList();
        var statistics = dtoList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();

        Map<String, Object> resultBody = Map.of(
                "result", dtoList,
                "summary", this.statisticsToSummaryMapper.apply(statistics)
        );
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "all")
                        .queryParam("short", "true"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=data&short=true")
    void handleGetStats_whenViewTypeIsData_shortInfoIsTrue_returnsOnlyDataInShortFormatWithNoSummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream()
                .map(stat -> TrackTimeDto.builder()
                        .methodName(stat.getMethodName())
                        .executionTime(stat.getExecutionTime())
                        .build())
                .toList();

        Map<String, Object> resultBody = Map.of("result", dtoList);
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "data")
                        .queryParam("short", "true"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tracktimestats/all?view=summary&short=true")
    void handleGetStats_whenViewTypeIsSummary_shortInfoIsTrue_returnsOnlySummary() throws Exception {
        when(this.timeStatsService.findAll()).thenReturn(this.trackTimeStats);

        List<TrackTimeDto> dtoList = this.trackTimeStats.stream()
                .map(stat -> TrackTimeDto.builder()
                        .methodName(stat.getMethodName())
                        .executionTime(stat.getExecutionTime())
                        .build())
                .toList();

        var statistics = dtoList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();

        Map<String, Object> resultBody = Map.of("summary", this.statisticsToSummaryMapper.apply(statistics));
        String jsonContent = this.objectMapper.writeValueAsString(resultBody);

        this.mockMvc.perform(get("/api/tracktimestats/all").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("view", "summary")
                        .queryParam("short", "true"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(jsonContent, true)
                );

        verify(this.timeStatsService, times(1)).findAll();
    }

    @Test
    @DisplayName("POST /api/tracktimestats/search :: json body is null")
    void handlePostSearch_whenRequestBodyIsNull_returnsErrorMessageSearchPropertiesMustBeSet() throws Exception {
        this.mockMvc.perform(post("/api/tracktimestats/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                  "statusCode": 400,
                                  "message": "At least one search property must be set"
                                }
                                """)
                );

        verifyNoInteractions(this.timeStatsService);
    }

    @Test
    @DisplayName("POST /api/tracktimestats/search :: empty json body")
    void handlePostSearch_whenRequestBodyIsEmpty_returnsErrorMessageSearchPropertiesMustBeSet() throws Exception {
        this.mockMvc.perform(post("/api/tracktimestats/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                  "statusCode": 400,
                                  "message": "At least one search property must be set"
                                }
                                """)
                );

        verifyNoInteractions(this.timeStatsService);
    }
}