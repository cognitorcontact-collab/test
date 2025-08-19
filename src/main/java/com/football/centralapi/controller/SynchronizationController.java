package com.football.centralapi.controller;

import com.football.centralapi.service.SynchronizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/synchronization")
public class SynchronizationController {

    private final SynchronizationService synchronizationService;

    public SynchronizationController(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    @PostMapping
    public ResponseEntity<String> synchronizeData() {
        synchronizationService.synchronizeAllData();
        return ResponseEntity.ok("Data synchronized successfully from all championship APIs");
    }
}