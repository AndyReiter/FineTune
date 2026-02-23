package com.finetune.app.exception;

import com.finetune.app.model.dto.ErrorResponse;
import com.finetune.app.model.dto.PublicWorkOrderCreationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handle(ResourceNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<PublicWorkOrderCreationResponse> handle(DailyLimitExceededException ex) {
        PublicWorkOrderCreationResponse errorResponse = PublicWorkOrderCreationResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }
}