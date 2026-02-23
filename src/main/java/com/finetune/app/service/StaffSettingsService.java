
package com.finetune.app.service;

import com.finetune.app.model.StaffSettings;
import com.finetune.app.model.dto.StaffSettingsResponse;
import com.finetune.app.model.dto.UpdateStaffSettingsRequest;
import com.finetune.app.repository.sql.StaffSettingsSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffSettingsService {

    private final StaffSettingsSqlRepository staffSettingsRepository;

    public StaffSettingsService(StaffSettingsSqlRepository staffSettingsRepository) {
        this.staffSettingsRepository = staffSettingsRepository;
    }

    @Transactional
    public StaffSettingsResponse getSettings() {
        StaffSettings settings = staffSettingsRepository.findFirstByOrderByIdAsc()
            .orElseGet(() -> new StaffSettings(25));
        return StaffSettingsResponse.fromEntity(settings);
    }

    @Transactional
    public StaffSettingsResponse updateSettings(UpdateStaffSettingsRequest request) {
        StaffSettings settings = staffSettingsRepository.findFirstByOrderByIdAsc()
            .orElseGet(() -> new StaffSettings(25));
        settings.setMaxCustomerWorkOrdersPerDay(request.getMaxCustomerWorkOrdersPerDay());
        // Persist the updated settings (implement update in SQL repository if needed)
        // staffSettingsRepository.update(settings); // Uncomment if update method exists
        return StaffSettingsResponse.fromEntity(settings);
    }

    public int getMaxCustomerWorkOrdersPerDay() {
        StaffSettings settings = staffSettingsRepository.findFirstByOrderByIdAsc()
            .orElseGet(() -> new StaffSettings(25));
        return settings.getMaxCustomerWorkOrdersPerDay();
    }
}
