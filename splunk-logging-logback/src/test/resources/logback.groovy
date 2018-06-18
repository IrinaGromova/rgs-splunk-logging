/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

import ru.rgs.logging.logback.InMemoryAppenderLogbackImpl
import ru.rgs.logging.logback.JsonLayout

appender("inMemoryAppender", InMemoryAppenderLogbackImpl) {
    layout(JsonLayout) {
        includeContextName = false
        includeLoggerName = false
        includeThreadName = false
        systemName = "self"
        environment = "testEnvironment"
        nodeName = "testNode"
        sparseLogging = false
        xmlToJsonFields = "XMLQueryBody"
        jsonFields = "JsonQueryBody"
    }
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-4relative [%thread] %-5level %logger{35} - %msg %n"
    }
}

logger("ru.rgs.logging.logback.tests", DEBUG, ["inMemoryAppender"], false)

root(INFO, ["STDOUT"])