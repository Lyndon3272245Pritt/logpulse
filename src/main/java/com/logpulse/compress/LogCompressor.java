package com.logpulse.compress;

import com.logpulse.model.LogEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Compresses serialized log entries using configurable strategies.
 * Supports GZIP and NONE (passthrough) compression modes.
 */
public class LogCompressor {

    private static final Logger logger = Logger.getLogger(LogCompressor.class.getName());

    private final CompressConfig config;

    public LogCompressor(CompressConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("CompressConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Compresses a single log entry's message field.
     * Returns the entry unchanged if compression is disabled or message is below threshold.
     */
    public LogEntry compress(LogEntry entry) {
        if (entry == null) {
            return null;
        }
        String message = entry.getMessage();
        if (!config.isEnabled() || message == null || message.length() < config.getMinSizeBytes()) {
            return entry;
        }
        try {
            String compressed = gzipAndEncode(message);
            return new LogEntry(
                    entry.getTimestamp(),
                    entry.getLevel(),
                    entry.getService(),
                    compressed,
                    entry.getFields()
            );
        } catch (IOException e) {
            logger.warning("Failed to compress log entry from service " + entry.getService() + ": " + e.getMessage());
            return entry;
        }
    }

    /**
     * Compresses a batch of log entries.
     */
    public List<LogEntry> compressBatch(List<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        List<LogEntry> result = new ArrayList<>(entries.size());
        for (LogEntry entry : entries) {
            result.add(compress(entry));
        }
        return result;
    }

    /**
     * Returns the number of bytes saved in the last compress call (approximate).
     */
    public long estimateSavings(String original) throws IOException {
        if (original == null || original.isEmpty()) {
            return 0L;
        }
        byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
        byte[] compressedBytes = gzipRaw(originalBytes);
        return Math.max(0L, originalBytes.length - compressedBytes.length);
    }

    private String gzipAndEncode(String input) throws IOException {
        byte[] compressed = gzipRaw(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(compressed);
    }

    private byte[] gzipRaw(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }

    public CompressConfig getConfig() {
        return config;
    }
}
