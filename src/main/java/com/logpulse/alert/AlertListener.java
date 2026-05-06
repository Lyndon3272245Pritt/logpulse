package com.logpulse.alert;

/**
 * Callback interface for receiving fired alert events from LogAlertEvaluator.
 */
@FunctionalInterface
public interface AlertListener {

    /**
     * Called when an alert rule threshold has been exceeded.
     *
     * @param event the alert event containing rule details and match count
     */
    void onAlert(AlertEvent event);
}
