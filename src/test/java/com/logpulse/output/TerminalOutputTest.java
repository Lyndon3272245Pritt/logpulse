package com.logpulse.output;

import com.logpulse.formatter.LogFormatter;
import com.logpulse.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalOutputTest {

    private LogFormatter mockFormatter;
    private ByteArrayOutputStream baos;
    private PrintStream printStream;

    @BeforeEach
    void setUp() {
        mockFormatter = mock(LogFormatter.class);
        baos = new ByteArrayOutputStream();
        printStream = new PrintStream(baos);
    }

    private LogEntry entry(String level) {
        return new LogEntry("svc-a", level, "Test message", Instant.now(), null);
    }

    @Test
    void write_incrementsLineCount() {
        when(mockFormatter.format(any())).thenReturn("formatted");
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, false);
        output.write(entry("INFO"));
        output.write(entry("ERROR"));
        assertEquals(2, output.getLineCount());
    }

    @Test
    void write_nullEntry_doesNothing() {
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, false);
        assertDoesNotThrow(() -> output.write(null));
        assertEquals(0, output.getLineCount());
    }

    @Test
    void write_colorDisabled_noAnsiCodes() {
        when(mockFormatter.format(any())).thenReturn("plain text");
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, false);
        output.write(entry("ERROR"));
        String result = baos.toString();
        assertFalse(result.contains("\u001B["));
        assertTrue(result.contains("plain text"));
    }

    @Test
    void write_colorEnabled_errorIsRed() {
        when(mockFormatter.format(any())).thenReturn("error line");
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, true);
        output.write(entry("ERROR"));
        String result = baos.toString();
        assertTrue(result.contains("\u001B[31m"));
    }

    @Test
    void writeBanner_outputsFormattedBanner() {
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, false);
        output.writeBanner("Starting svc-a");
        assertTrue(baos.toString().contains("=== Starting svc-a ==="));
    }

    @Test
    void resetLineCount_resetsToZero() {
        when(mockFormatter.format(any())).thenReturn("line");
        TerminalOutput output = new TerminalOutput(printStream, mockFormatter, false);
        output.write(entry("INFO"));
        output.write(entry("WARN"));
        output.resetLineCount();
        assertEquals(0, output.getLineCount());
    }

    @Test
    void constructor_nullFormatter_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TerminalOutput(printStream, null, false));
    }
}
