package com.logpulse.window;

import java.time.Duration;

/**
 * Configuration for log windowing — groups log entries into fixed or sliding time windows.
 */
public class WindowConfig {

    public enum WindowType {
        FIXED, SLIDING
    }

    private final WindowType windowType;
    private final Duration windowSize;
    private final Duration slideInterval;
    private final int maxEntriesPerWindow;

    private WindowConfig(Builder builder) {
        this.windowType = builder.windowType;
        this.windowSize = builder.windowSize;
        this.slideInterval = builder.slideInterval;
        this.maxEntriesPerWindow = builder.maxEntriesPerWindow;
    }

    public WindowType getWindowType() { return windowType; }
    public Duration getWindowSize() { return windowSize; }
    public Duration getSlideInterval() { return slideInterval; }
    public int getMaxEntriesPerWindow() { return maxEntriesPerWindow; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private WindowType windowType = WindowType.FIXED;
        private Duration windowSize = Duration.ofSeconds(10);
        private Duration slideInterval = Duration.ofSeconds(5);
        private int maxEntriesPerWindow = 1000;

        public Builder windowType(WindowType windowType) {
            this.windowType = windowType;
            return this;
        }

        public Builder windowSize(Duration windowSize) {
            if (windowSize == null || windowSize.isNegative() || windowSize.isZero()) {
                throw new IllegalArgumentException("windowSize must be positive");
            }
            this.windowSize = windowSize;
            return this;
        }

        public Builder slideInterval(Duration slideInterval) {
            if (slideInterval == null || slideInterval.isNegative() || slideInterval.isZero()) {
                throw new IllegalArgumentException("slideInterval must be positive");
            }
            this.slideInterval = slideInterval;
            return this;
        }

        public Builder maxEntriesPerWindow(int max) {
            if (max <= 0) throw new IllegalArgumentException("maxEntriesPerWindow must be > 0");
            this.maxEntriesPerWindow = max;
            return this;
        }

        public WindowConfig build() {
            if (windowType == WindowType.SLIDING && slideInterval.compareTo(windowSize) >= 0) {
                throw new IllegalArgumentException("slideInterval must be less than windowSize for SLIDING windows");
            }
            return new WindowConfig(this);
        }
    }
}
