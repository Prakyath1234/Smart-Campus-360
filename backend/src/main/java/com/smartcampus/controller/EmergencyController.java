package com.smartcampus.controller;

import com.smartcampus.dto.EmergencyAlertDto;
import com.smartcampus.dto.EmergencyAnalyticsDto;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.EmergencyAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyAlertService emergencyAlertService;

    public EmergencyController(EmergencyAlertService emergencyAlertService) {
        this.emergencyAlertService = emergencyAlertService;
    }

    @PostMapping({"/trigger", "/sos"})
    public ResponseEntity<EmergencyAlertDto> triggerSos(@RequestBody EmergencyAlertDto dto, Principal principal) {
        if (principal != null) {
            return ResponseEntity.ok(emergencyAlertService.triggerSosForEmail(dto, principal.getName()));
        } else if (dto.getStudentId() != null) {
            return ResponseEntity.ok(emergencyAlertService.triggerSos(dto));
        } else {
            throw new BadRequestException("Unauthenticated user cannot trigger emergency SOS without student ID");
        }
    }

    @GetMapping({"", "/alerts", "/history"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<List<EmergencyAlertDto>> getAllAlerts() {
        return ResponseEntity.ok(emergencyAlertService.getAllAlerts());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<List<EmergencyAlertDto>> getActiveAlerts() {
        return ResponseEntity.ok(emergencyAlertService.getActiveAlerts());
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<EmergencyAnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(emergencyAlertService.getEmergencyAnalytics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyAlertDto> getAlertById(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            emergencyAlertService.verifySosAccess(id, principal.getName());
        }
        return ResponseEntity.ok(emergencyAlertService.getAlertById(id));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<EmergencyAlertDto> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "ACKNOWLEDGED", null));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<EmergencyAlertDto> startResponse(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "IN_PROGRESS", null));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<EmergencyAlertDto> resolveAlert(@PathVariable Long id, @RequestParam(required = false) Long resolverUserId) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "RESOLVED", resolverUserId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EmergencyAlertDto> cancelAlert(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            emergencyAlertService.verifySosAccess(id, principal.getName());
        }
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "CANCELLED", null));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECURITY')")
    public ResponseEntity<EmergencyAlertDto> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) Long resolverUserId) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, status, resolverUserId));
    }
}
