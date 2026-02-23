package com.finetune.app.exception;

/**
 * Exception thrown when a customer exceeds their daily work order creation limit.
 */
public class DailyLimitExceededException extends RuntimeException {
    
    public DailyLimitExceededException(String message) {
        super(message);
    }
    
    public DailyLimitExceededException(int limit) {
        super("You have reached your daily limit of " + limit + " work orders. Please visit the shop for further service.");
    }
}
