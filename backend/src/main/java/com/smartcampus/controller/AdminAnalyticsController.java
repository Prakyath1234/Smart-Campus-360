package com.smartcampus.controller;

import com.smartcampus.dto.EmergencyAnalyticsDto;
import com.smartcampus.service.AdminAnalyticsService;
import com.smartcampus.service.EmergencyAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;
    private final EmergencyAlertService emergencyAlertService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService,
                                    EmergencyAlertService emergencyAlertService) {
        this.adminAnalyticsService = adminAnalyticsService;
        this.emergencyAlertService = emergencyAlertService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewAnalytics() {
        return ResponseEntity.ok(adminAnalyticsService.getOverviewAnalytics());
    }

    @GetMapping("/emergency")
    public ResponseEntity<EmergencyAnalyticsDto> getEmergencyAnalytics() {
        return ResponseEntity.ok(emergencyAlertService.getEmergencyAnalytics());
    }
}
