/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

import ch.qos.logback.classic.Level
import groovy.transform.BaseScript
import ru.rgs.logging.logback.LogbackBaseInit

enum Loggers {
    businessOperationLogger
}

enum Appenders {
    SplunkBusinessAppender,
}

def init = {
    @BaseScript LogbackBaseInit logbackBaseInitScript
//    splunkAppenders(["$Appenders.SplunkBusinessAppender": businessLogsIndex]) // This is also working
    splunkAppenders(["$Appenders.SplunkBusinessAppender": [index: businessLogsIndex, xmlFields: 'xml1', jsonFields: 'json1,json2,json3']])

    attachSplunkAppenders({
        root(logLevel, ["$Appenders.SplunkBusinessAppender"])

        logger("$Loggers.businessOperationLogger", Level.INFO, ["$Appenders.SplunkBusinessAppender"], false)
    })
}

init()
