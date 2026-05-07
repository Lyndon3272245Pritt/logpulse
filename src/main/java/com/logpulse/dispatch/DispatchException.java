package com.logpulse.dispatch;

/**
 * Thrown when a dispatch operation fails after all retry attempts.
 */
public class DispatchException extends RuntimeException {

    private final String targetName;

    public DispatchException(String targetName, Throwable cause) {
        super("Dispatch failed for target '" + targetName + "': " + cause.getMessage(), cause);
        this.targetName = targetName;
    }

    public String getTargetName() {
        return targetName;
    }
}
