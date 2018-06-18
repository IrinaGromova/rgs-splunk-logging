/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited. 
 */

package ru.rgs.logging.logback

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.slf4j.LoggerFactory
import ru.rgs.logging.core.InMemoryAppender
import ru.rgs.logging.core.Slf4jLoggingUtils

/**
 * @author jihor (jihor@ya.ru)
 *         Created on 2017-05-12
 */
class LogbackLoggingUtils extends Slf4jLoggingUtils {

    LogbackLoggingUtils(String system, String appenderName, boolean validate) {
        super(system, appenderName, validate)
    }

    LogbackLoggingUtils(String system, String appenderName) {
        this(system, appenderName, true)
    }

    LogbackLoggingUtils(String system) {
        this(system, "inMemoryAppender")
    }

    @Override
    protected InMemoryAppender getAppender() {
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory()
        for (Logger logger in context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Appender<ILoggingEvent> appender = index.next()
                if (getAppenderName().equals(appender.getName())) {
                    return appender
                }
            }
        }
        throw new RuntimeException("Appender named ${getAppenderName()} was not found in Logback logging context. Is logging configured properly?")
    }
}
