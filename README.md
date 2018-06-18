# ru.rgs.dsa-logging
Logging library for Splunk. Includes:
- [Logging convention](http://confluence.rgs.ru:8080/display/Microservices/Logging+Convention) compatible layouts for Log4j2 and Logback
- In-memory appenders for Log4j2 and Logback for testing 

<br/><br/>


---
## Production setup
### Log4j2
#### Imports
```
compile "ru.rgs.logging:splunk-logging-log4j2:<version>"
```
#### log4j2.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <File fileName="service.log" name="fileTechAppender">
            <PatternLayout pattern="%d{dd MMM yyyy HH:mm:ss,SSS} - %t %-5p %c{2} - %m%n"/>
        </File>
        <File fileName="business.log" name="fileBusinessAppender">
            <PatternLayout pattern="%d{dd MMM yyyy HH:mm:ss,SSS} - %t %-5p %c{2} - %m%n"/>
        </File>
        <Http name="splunkTechAppender"
              url="http://splunk-host:port"
              token="[token]"
              host="insbridge_host"
              index="[index-name]"
              includeLoggerName="false"
              includeThreadName="false"
              includeMDC="false"
              includeException="false"
              includeMarker="false"
              sourcetype="json-escaped">
            <SplunkJsonLayout systemName="[system-name]"
                              environment="[environment-name]"
                              nodeName="[node-name]"
                              includeThreadName="false"
                              includeLoggerName="false"
                              sparseLogging="true"
                              jsonFields="JsonQueryBody,AnotherJsonField"
                              xmlToJsonFields="XMLQueryBody"/>
        </Http>
        <Http name="splunkBusinessAppender"
              url="http://splunk-host:port"
              token="[token]"
              host="insbridge_host"
              index="businessoperations"
              includeLoggerName="false"
              includeThreadName="false"
              includeMDC="false"
              includeException="false"
              includeMarker="false"
              sourcetype="json-escaped">
            <SplunkJsonLayout systemName="[system-name]"
                              environment="[environment-name]"
                              nodeName="[node-name]"
                              includeThreadName="false"
                              includeLoggerName="false"
                              sparseLogging="true"
                              jsonFields="JsonQueryBody,AnotherJsonField"
                              xmlToJsonFields="QueryBody"/>
        </Http>
    </Appenders>
    <Loggers>
        <Logger level="warn" name="ru.rgs" additivity="false">
            <AppenderRef ref="splunkTechAppender"/>
            <AppenderRef ref="fileTechAppender"/>
        </Logger>
        <Logger level="info" name="businessOperationLogger" additivity="false">
            <AppenderRef ref="splunkBusinessAppender"/>
            <AppenderRef ref="fileBusinessAppender"/>
        </Logger>
        <Root level="warn">
            <AppenderRef ref="splunkTechAppender"/>
            <AppenderRef ref="fileTechAppender"/>
        </Root>
    </Loggers>
</Configuration>
```
   
### Logback
#### Imports
```
compile "ru.rgs.logging:splunk-logging-logback:<version>"
```
#### logback.groovy

```
import ch.qos.logback.classic.Level
import groovy.transform.BaseScript
import ru.rgs.logging.logback.LogbackBaseInit

enum Loggers {
    businessOperationLogger, slowQueryLogger
}

enum Appenders {
    SplunkTechAppender, SplunkBusinessAppender, SplunkSlowqueryAppender,
    FileTechAppender, FileBusinessAppender, FileSlowqueryAppender
}

def init = {
    @BaseScript LogbackBaseInit logbackBaseInitScript
    splunkAppenders(["$Appenders.SplunkTechAppender"        : logsIndex,
                     "$Appenders.SplunkBusinessAppender"    : [index: businessLogsIndex, xmlFields: 'XMLQueryBody', jsonFields: 'JsonQueryBody,AnotherJsonField'],
                     "$Appenders.SplunkSlowqueryAppender"   : slowQueryLogsIndex])
    fileAppenders(["$Appenders.FileTechAppender"      : [filename: "service.log", pattern: "%d{MM.dd-HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"],
                   "$Appenders.FileBusinessAppender"  : [filename: "business.log", pattern: "%d{MM.dd-HH:mm:ss.SSS} CorrelationId: %X{CorrelationId} Action: %X{Action} System: %X{System} - %msg%n"],
                   "$Appenders.FileSlowqueryAppender" : [filename: "slowquery.log", pattern: "%d{MM.dd-HH:mm:ss.SSS} CorrelationId: %X{CorrelationId} Action: %X{Action} System: %X{System} Method: %X{Method} - %msg%n"]])

    attachSplunkAppenders({
        root(logLevel, ["$Appenders.SplunkTechAppender"])

      // prevent deadlocking and OOM errors on levels finer or equal to DEBUG with Splunk
        logger("org.apache.http", Level.INFO, null, true)
        // if DEBUG or finer logging must be enabled on org.apache.http package, it must be either
        // done to file with additivity=false,
        // or Splunk logging must be turned off at all during that period (enable.log.to.splunk -> false)

        logger("$Loggers.slowQueryLogger", Level.INFO, ["$Appenders.SplunkSlowqueryAppender"], false)
        logger("$Loggers.businessOperationLogger", Level.INFO, ["$Appenders.SplunkBusinessAppender"], false)
    })

    attachFileAppenders({
        root(logLevel, ["$Appenders.FileTechAppender"]);

        logger("$Loggers.businessOperationLogger", Level.INFO, ["$Appenders.FileBusinessAppender"], false)
        logger("$Loggers.slowQueryLogger", Level.INFO, ["$Appenders.FileSlowqueryAppender"], false)
    })
}

init()

```
#### Dockerfile settings
```
-Dlogging.tech.index.name=$LOGGING_TECH_INDEX_NAME \
-Dlogging.business.index.name=$LOGGING_BUSINESS_INDEX_NAME \
-Dlogging.slowquery.index.name=$LOGGING_SLOWQUERY_INDEX_NAME \
-Dlogging.directory=$LOGGING_DIRECTORY \
-Dlogging.enable.log.to.file=$LOGGING_ENABLE_LOG_TO_FILE \
-Dlogging.enable.log.to.splunk=$LOGGING_ENABLE_LOG_TO_SPLUNK \
-Dlogging.environment=$LOGGING_ENVIRONMENT \
-Dlogging.level=$LOGGING_LEVEL \
-Dlogging.nodeName=$LOGGING_NODE \
-Dlogging.system.name=$LOGGING_SYSTEM_NAME \
-Dlogging.sparse=$LOGGING_SPARSE \
-Dlogging.splunk.host=$LOGGING_SPLUNK_HOST \
-Dlogging.splunk.port=$LOGGING_SPLUNK_PORT \
-Dlogging.splunk.token=$LOGGING_SPLUNK_TOKEN \
``` 
<br/><br/>


---

## Testing setup
### Log4j2 
#### Imports
```
testCompile "ru.rgs.logging:logging-test-inmemory-log4j2:<version>"
```
#### log4j2.xml
See `splunk-logging-log4j2/src/test/resources/log4j2.xml`
#### Usage in tests
See `ru.rgs.logging.log4j2.tests.Log4j2LayoutTest`

### Logback
#### Imports
```
testCompile "ru.rgs.logging:logging-test-inmemory-logback:<version>"
```
#### logback.groovy
See `splunk-logging-logback/src/test/resources/logback.groovy`
#### Usage in tests
See `ru.rgs.logging.logback.tests.LogbackLayoutTest`  
<br/><br/>


---

## Supported Layout and Appender properties
| Property | Support for Log4j2 | Support for Logback | Corresponding field in Logging convention| Format | Note |
|------------|------------------------|------------------------------|------------------------------------------|---------|------|
| systemName  | Yes  | Yes | `message.System` | string | |
| environment  | Yes  | Yes | `message.Environment` | string | |
| nodeName  | Yes  | Yes | `message.Node` | string | |
| sparseLogging  | Yes  | Yes | N/A | boolean | Option for reducing the logged events size. Turning the sparse logging on by setting this option to `true` means that all non-retained fields are deleted from MDC when the event is logged. Also, `message.ext.Thread` and `message.ext.Logger` will never be logged.  Defaults to `true` |
| retainedMDCFields  | Yes  | Yes | N/A | comma-separated strings | Use with `sparseLogging`. CORRELATION_ID and ACTION_ID are always retained |
| standardMessageFields  | Yes  | Yes | N/A | comma-separated strings | List of additional fields to be stored at the root level (all other fields will end up in `ext` block). All top-level fields described in the Logging convention are always included |
| xmlToJsonFields  | Yes  | Yes | N/A | comma-separated strings | List of fields to be treated as xml strings and converted to json subtrees of the event. Defaults to an empty list for lo4j2 implementation and contains 'xmlField' value for logback implementation |
| jsonFields  | Yes  | Yes | N/A | comma-separated strings | List of fields to be converted to json subtrees of the event. Defaults to an empty list for lo4j2 implementation and contains 'jsonField' value for logback implementation| includeThreadName  | Yes  | Always `true` | `message.ext.Thread` | boolean | Controls whether to include the thread name in the event. Defaults to `true`, ignored when sparse logging in on. |
| includeLoggerName  | Yes  | Always `true`  | `message.ext.Logger` | boolean | Controls whether to include the logger name in the event. Defaults to `true`, ignored when sparse logging in on. |
| includeMDC  | Yes  | Yes | `message.ext.*` | boolean | Controls whether to include the thread name in the event. Defaults to `true` |
| includeMessage  | Yes  | Yes | `message.Message` | boolean | Controls whether to include the message text in the event. Defaults to `true` |
| includeException  | Yes  | Yes| `message.ext.Exception` | boolean | Controls whether to include the exception in the event. Defaults to `true` |