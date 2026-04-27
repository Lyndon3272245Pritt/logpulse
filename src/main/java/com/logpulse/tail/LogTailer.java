package com.logpulse.tail;

import com.logpulse.model.LogEntry;
import com.logpulse.parser.LogEntryParser;

import java.io.*;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Tails a log file and emits parsed LogEntry objects to a registered consumer.
 * Supports graceful shutdown via {@link #stop()}.
 */
public class LogTailer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(LogTailer.class.getName());
    private static final long POLL_INTERVAL_MS = 250;

    private final Path filePath;
    private final LogEntryParser parser;
    private final Consumer<LogEntry> entryConsumer;
    private volatile boolean running;

    public LogTailer(Path filePath, LogEntryParser parser, Consumer<LogEntry> entryConsumer) {
        if (filePath == null) throw new IllegalArgumentException("filePath must not be null");
        if (parser == null) throw new IllegalArgumentException("parser must not be null");
        if (entryConsumer == null) throw new IllegalArgumentException("entryConsumer must not be null");
        this.filePath = filePath;
        this.parser = parser;
        this.entryConsumer = entryConsumer;
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            // Start at the end of the file to tail only new lines
            raf.seek(raf.length());

            while (running) {
                String line = raf.readLine();
                if (line != null) {
                    parser.parse(line).ifPresent(entryConsumer);
                } else {
                    Thread.sleep(POLL_INTERVAL_MS);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.severe("Log file not found: " + filePath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("LogTailer interrupted for: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("I/O error while tailing " + filePath + ": " + e.getMessage());
        } finally {
            running = false;
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public Path getFilePath() {
        return filePath;
    }
}
