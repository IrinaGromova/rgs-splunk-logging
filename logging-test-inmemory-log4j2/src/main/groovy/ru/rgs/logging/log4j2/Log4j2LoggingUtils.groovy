/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.log4j2

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import ru.rgs.logging.core.InMemoryAppender
import ru.rgs.logging.core.Slf4jLoggingUtils

/**
 * @author jihor (jihor@ya.ru)
 *         Created on 2017-05-12
 */
class Log4j2LoggingUtils extends Slf4jLoggingUtils {

    Log4j2LoggingUtils(String system, String appenderName, boolean validate) {
        super(system, appenderName, validate)

    }

    Log4j2LoggingUtils(String system, String appenderName) {
        this(system, appenderName, true)
    }

    Log4j2LoggingUtils(String system) {
        this(system, "inMemoryAppender")
    }

    @Override
    protected InMemoryAppender getAppender() {
        LoggerContext context = (LoggerContext) LogManager.getContext()
        Object appender = context.getConfiguration().getAppender(getAppenderName())
        if (appender) {
            return (InMemoryAppender) appender
        } else {
            throw new RuntimeException("Appender named ${getAppenderName()} was not found in Logback logging context. Is logging configured properly?")
        }
    }
}
