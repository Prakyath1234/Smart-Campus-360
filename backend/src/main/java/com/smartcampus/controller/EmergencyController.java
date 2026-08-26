package com.smartcampus.controller;

import com.smartcampus.dto.EmergencyAlertDto;
import com.smartcampus.service.EmergencyAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyAlertService emergencyAlertService;

    public EmergencyController(EmergencyAlertService emergencyAlertService) {
        this.emergencyAlertService = emergencyAlertService;
    }

    @PostMapping("/sos")
    public ResponseEntity<EmergencyAlertDto> triggerSos(@RequestBody EmergencyAlertDto dto) {
        return ResponseEntity.ok(emergencyAlertService.triggerSos(dto));
    }

    @GetMapping
    public ResponseEntity<List<EmergencyAlertDto>> getAllAlerts() {
        return ResponseEntity.ok(emergencyAlertService.getAllAlerts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmergencyAlertDto>> getActiveAlerts() {
        return ResponseEntity.ok(emergencyAlertService.getActiveAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyAlertDto> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.getAlertById(id));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<EmergencyAlertDto> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "ACKNOWLEDGED", null));
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<EmergencyAlertDto> startResponse(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "IN_PROGRESS", null));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<EmergencyAlertDto> resolveAlert(@PathVariable Long id, @RequestParam Long resolverUserId) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "RESOLVED", resolverUserId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EmergencyAlertDto> cancelAlert(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyAlertService.updateAlertStatus(id, "CANCELLED", null));
    }
}
