/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core

abstract class Slf4jLoggingUtils extends AbstractLoggingUtils {
    private final InMemoryAppender IN_MEMORY_APPENDER

    String environment
    String node
    String system
    String appenderName
    Boolean validate

    Slf4jLoggingUtils(String system, String appenderName, boolean validate) {
        environment = TestConstants.ENVIRONMENT
        node = TestConstants.NODE
        this.appenderName = appenderName
        this.system = system
        this.validate = validate
        IN_MEMORY_APPENDER = getAppender()
    }

    protected abstract InMemoryAppender getAppender()

    @Override
    List<CanonicalLogEvent> events() {
        def events = IN_MEMORY_APPENDER.getEvents()
        if (validate) {
            // We cannot validate events at log time (appender will simply skip them in case of validation failure)
            // so we have to validate when checking if they have been properly logged
            events.each {
                it.validate()
            }
        }
        return events
    }

    @Override
    void clearEvents() {
        IN_MEMORY_APPENDER.clearEvents()
    }
}