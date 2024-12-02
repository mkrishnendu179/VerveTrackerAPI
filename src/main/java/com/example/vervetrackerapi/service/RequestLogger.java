package com.example.vervetrackerapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RequestLogger {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    private final Set<Integer> uniqueIds = ConcurrentHashMap.newKeySet();
    private final WebClient webClient = WebClient.create();

    public void addUniqueId (int id, String endpoint){
        uniqueIds.add(id);

        // If endpoint is provided, fire the HTTP POST request asynchronously
        if (endpoint != null) {
            int uniqueRequestCount = uniqueIds.size();
            fireHttpPostRequestToEndpoint(endpoint, uniqueRequestCount);
        }
    }
    // Fire HTTP POST request to the provided endpoint with the unique request count and timestamp
    private void fireHttpPostRequestToEndpoint(String endpoint, int uniqueRequestCount) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("timestamp", LocalDateTime.now().toString());
        postData.put("uniqueRequestCount", uniqueRequestCount);

        webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(postData)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> logger.info("Fired POST request to {}, Status Code: {}", endpoint, response.getStatusCode()))
                .doOnError(error -> logger.error("Error firing POST request to {}: {}", endpoint, error.getMessage()))
                .subscribe();
    }

    @Scheduled(fixedRate = 60000) // Schedule to run every minute
    public void logUniqueRequestCount() {
        int uniqueRequestCount = uniqueIds.size();
        logger.info("Unique requests in the last minute: {}", uniqueRequestCount);

        // Clear the set for the next minute
        uniqueIds.clear();
    }
}

