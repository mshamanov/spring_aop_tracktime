package com.mash.aoptracktime.rest.controller;

import com.mash.aoptracktime.entity.TrackTimeStat;
import com.mash.aoptracktime.rest.mapper.LongStatisticsToSummaryMapper;
import com.mash.aoptracktime.rest.mapper.TrackTimeDtoToSpecificationMapper;
import com.mash.aoptracktime.rest.mapper.TrackTimeEntityToDtoMapper;
import com.mash.aoptracktime.rest.model.TrackTimeDto;
import com.mash.aoptracktime.service.TrackTimeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Rest controller to retrieve method execution time measurements.
 *
 * @author Mikhail Shamanov
 */
@RestController
@RequestMapping(path = "/api/v1/tracktime")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrackTimeRestController {
    /**
     * Types of measurement data representation.
     */
    public static final class ViewTypes {
        /**
         * Data and summary included
         */
        public static final String ALL = "ALL";

        /**
         * Only data included
         */
        public static final String DATA = "DATA";

        /**
         * Only summary included
         */
        public static final String SUMMARY = "SUMMARY";
    }

    private final TrackTimeStatsService trackTimeStatsService;

    private final TrackTimeDtoToSpecificationMapper toSpecificationMapper;
    private final TrackTimeEntityToDtoMapper toDtoMapper;
    private final LongStatisticsToSummaryMapper statisticsToSummaryMapper;

    @GetMapping("/stats")
    public ResponseEntity<?> getTrackTimeStats(@RequestParam(name = "view", defaultValue = "all") String viewType,
                                               @RequestParam(name = "short", defaultValue = "false") boolean shortFormat) {
        List<TrackTimeStat> timeStats = this.trackTimeStatsService.findAll();
        return this.prepareResponse(timeStats, viewType, shortFormat);
    }

    @PostMapping("/stats")
    public ResponseEntity<?> searchTrackTimeStats(@RequestBody(required = false) TrackTimeDto requestDto,
                                                  @RequestParam(name = "view", defaultValue = "all") String viewType,
                                                  @RequestParam(name = "short", defaultValue = "false") boolean shortFormat) {
        if (requestDto == null || TrackTimeDto.isAllNull(requestDto)) {
            throw new IllegalStateException("At least one search property must be specified");
        }

        List<TrackTimeStat> timeStats = this.trackTimeStatsService.findAll(this.toSpecificationMapper.apply(requestDto));
        return this.prepareResponse(timeStats, viewType, shortFormat);
    }

    /**
     * Prepares response for the client.
     *
     * @param data        measurement data as a collection
     * @param viewType    type of data representation {@link ViewTypes}
     * @param shortFormat true if a single measurement data of a method call should be in a short format, otherwise false
     * @return response
     */
    private ResponseEntity<Map<String, Object>> prepareResponse(Collection<? extends TrackTimeStat> data,
                                                                String viewType, boolean shortFormat) {

        Function<TrackTimeStat, TrackTimeDto> toDtoMapper;
        if (shortFormat) {
            toDtoMapper = this.toDtoMapper.toShort();
        } else {
            toDtoMapper = this.toDtoMapper.toNormal();
        }

        List<TrackTimeDto> resultList = data.stream().map(toDtoMapper).toList();
        var statistics = resultList.stream().mapToLong(TrackTimeDto::getExecutionTime).summaryStatistics();
        var summary = this.statisticsToSummaryMapper.apply(statistics);

        Map<String, Object> body = new HashMap<>();

        if (viewType.isBlank() || viewType.equalsIgnoreCase(ViewTypes.ALL)) {
            body.put("result", resultList);
            body.put("summary", summary);
        } else if (viewType.equalsIgnoreCase(ViewTypes.DATA)) {
            body.put("result", resultList);
        } else if (viewType.equalsIgnoreCase(ViewTypes.SUMMARY)) {
            body.put("summary", summary);
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
