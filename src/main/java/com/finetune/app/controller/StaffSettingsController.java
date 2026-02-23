package com.finetune.app.controller;

import com.finetune.app.model.dto.StaffSettingsResponse;
import com.finetune.app.model.dto.UpdateStaffSettingsRequest;
import com.finetune.app.service.StaffSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * StaffSettingsController handles API endpoints for managing global staff settings.
 */
@RestController
@RequestMapping("/api/staff-settings")
@CrossOrigin(origins = "*")
public class StaffSettingsController {

    private final StaffSettingsService staffSettingsService;

    public StaffSettingsController(StaffSettingsService staffSettingsService) {
        this.staffSettingsService = staffSettingsService;
    }

    /**
     * Get the current staff settings.
     * 
     * @return Current staff settings
     */
    @GetMapping
    public ResponseEntity<StaffSettingsResponse> getSettings() {
        StaffSettingsResponse settings = staffSettingsService.getSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Update the staff settings.
     * 
     * @param request Updated settings values
     * @return Updated staff settings
     */
    @PutMapping
    public ResponseEntity<StaffSettingsResponse> updateSettings(@Valid @RequestBody UpdateStaffSettingsRequest request) {
        StaffSettingsResponse settings = staffSettingsService.updateSettings(request);
        return ResponseEntity.ok(settings);
    }
}
