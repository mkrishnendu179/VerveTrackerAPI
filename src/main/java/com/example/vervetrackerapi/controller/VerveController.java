package com.example.vervetrackerapi.controller;

import com.example.vervetrackerapi.service.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/verve")
public class VerveController {

    private static final Logger logger = LoggerFactory.getLogger(VerveController.class);

    @Autowired
    RequestLogger requestLogger;

    @GetMapping("/accept")
    public ResponseEntity<String> acceptRequest(
            @RequestParam("id") int id,
            @RequestParam(value = "endpoint", required = false) String endpoint) {

        try {
            requestLogger.addUniqueId(id,endpoint);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
    }


}

