package com.smartcampus.controller;

import com.smartcampus.dto.ServiceRequestDto;
import com.smartcampus.service.ServiceRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping("/student/service-requests")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ServiceRequestDto> createRequest(@RequestBody ServiceRequestDto dto, Principal principal) {
        return ResponseEntity.ok(serviceRequestService.createRequest(dto, principal.getName()));
    }

    @GetMapping("/student/service-requests")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<List<ServiceRequestDto>> getStudentRequests(Principal principal) {
        return ResponseEntity.ok(serviceRequestService.getStudentRequests(principal.getName()));
    }

    @GetMapping("/admin/service-requests")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<ServiceRequestDto>> getAdminRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(serviceRequestService.getAdminRequests(search, requestType, status, pageable));
    }

    @PutMapping("/admin/service-requests/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ServiceRequestDto> updateStatus(@PathVariable Long id, @RequestParam String status,
                                                           @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(serviceRequestService.updateStatus(id, status, comments));
    }
}
