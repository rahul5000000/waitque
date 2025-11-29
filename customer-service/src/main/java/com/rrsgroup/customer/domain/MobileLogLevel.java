package com.rrsgroup.customer.domain;

import org.apache.logging.log4j.Level;

public enum MobileLogLevel {
    DEBUG(Level.DEBUG),
    INFO(Level.INFO),
    ERROR(Level.ERROR);

    private final Level level;

    MobileLogLevel(Level level) {
        this.level = level;
    }

    public Level getLog4jLevel() {
        return level;
    }
}
