package dev.isswmq.digitalchiefproject.controller;

import dev.isswmq.digitalchiefproject.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductIndexingController {

    private final ProductIndexingService dataLoadService;

    @GetMapping("/reindex")
    public ResponseEntity<String> loadDataToElastic(@RequestParam(value = "date", required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Starting data load to Elasticsearch for date: {}", date);
            dataLoadService.loadDataToElastic(date);
            log.info("Data loaded successfully");
            return ResponseEntity.ok("Data loaded successfully");
        } catch (IOException e) {
            log.error("Error occurred during data load: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during data load");
        }
    }
}
