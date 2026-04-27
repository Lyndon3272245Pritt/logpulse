package com.logpulse.aggregator;

import com.logpulse.filter.LogFilter;
import com.logpulse.parser.LogEntryParser;
import com.logpulse.tail.LogTailer;

import java.io.File;

/**
 * Factory that constructs a fully-wired {@link LogAggregator} from an
 * {@link AggregatorConfig}.
 */
public class AggregatorFactory {

    private final LogEntryParser parser;

    public AggregatorFactory(LogEntryParser parser) {
        this.parser = parser;
    }

    /**
     * Builds and returns a configured {@link LogAggregator}.
     *
     * @param config the aggregator configuration
     * @return a ready-to-start aggregator
     */
    public LogAggregator create(AggregatorConfig config) {
        LogFilter filter = buildFilter(config);
        LogAggregator aggregator = new LogAggregator(filter);

        for (String path : config.getFilePaths()) {
            File file = new File(path);
            String serviceName = deriveServiceName(file);
            LogTailer tailer = new LogTailer(file, serviceName, parser);
            aggregator.addTailer(tailer);
        }

        return aggregator;
    }

    private LogFilter buildFilter(AggregatorConfig config) {
        if (config.getLevelFilter() == null && config.getServiceFilter() == null) {
            return null;
        }
        return new LogFilter(config.getLevelFilter(), config.getServiceFilter());
    }

    private String deriveServiceName(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
