package com.finetune.app.model.dto;

import java.util.UUID;

public record WorkOrderResponse(
        UUID id,
        String status
) {}