/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.logback;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import org.slf4j.MDC;
import ru.rgs.logging.LoggingConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static ru.rgs.logging.Converter.xmlAsDataStructure;
import static ru.rgs.logging.Converter.jsonAsDataStructure;

/**
 * @author Nesterenko Maxim (maksim_nesterenko@rgs.ru)
 */
public class JsonLayout extends LayoutBase<ILoggingEvent> {

    protected boolean includeContextName = true;
    protected boolean includeThreadName = true;
    protected boolean includeLoggerName = true;
    protected boolean includeException = true;
    protected boolean includeMessage = true;
    protected boolean includeMDC = true;
    protected boolean includeRawMessage;
    protected boolean sparseLogging;
    protected String environment;
    protected String systemName;
    protected String nodeName;
    protected String xmlToJsonFields;
    protected String jsonFields;

    private Map<String, List<String>> fieldsCache = new HashMap<>();

    private final ThrowableProxyConverter throwableProxyConverter = new ThrowableProxyConverter();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();
    private static final Predicate<String> NON_EMPTY_STRING = ((Predicate<String>) String::isEmpty).negate();

    // only these fields will be retained in MDC after logging if sparse logging is enabled
    List<String> retainedMDCFields = ImmutableList.of(LoggingConstants.CORRELATION_ID, LoggingConstants.ACTION_ID);

    // only these fields will be written directly to event, all others will be written into extension section
    List<String> standardMessageFields = ImmutableList.of(LoggingConstants.CORRELATION_ID,
                                                          LoggingConstants.ACTION_ID,
                                                          LoggingConstants.ACTION_NAME,
                                                          LoggingConstants.BUSINESS_OPERATION_CODE,
                                                          LoggingConstants.BUSINESS_OPERATION_NAME,
                                                          LoggingConstants.SYSTEM_NAME,
                                                          LoggingConstants.MESSAGE,
                                                          LoggingConstants.ENVIRONMENT,
                                                          LoggingConstants.NODE);

    public JsonLayout() {
        throwableProxyConverter.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        throwableProxyConverter.stop();
    }

    private List<String> fieldsOf(String fields) {
        if (isNullOrEmpty(fields)) {
            return Collections.emptyList();
        }

        return fieldsCache.computeIfAbsent(fields, v -> Arrays.stream(v.split(",")).map(String::trim).collect(collectingAndThen(toList(), Collections::unmodifiableList)));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        try {
            return objectWriter.writeValueAsString(eventToMap(event));
        } catch (IOException e) {
            return "{"
                    + "\"" + LoggingConstants.SYSTEM_NAME + "\":\"" + systemName + "\","
                    + "\"" + LoggingConstants.NODE + "\":\"" + nodeName + "\","
                    + "\"" + LoggingConstants.ENVIRONMENT + "\":\"" + environment + "\","
                    + "\"" + LoggingConstants.MESSAGE + "\":\"" + "Exception occurred on creating log message: " + throwableProxyConverter.convert(event) + ", "
                    + "exception was: " + e.getMessage()
                    + "\"}";
        }
    }

    protected Map eventToMap(ILoggingEvent event) {
        Map<String, Object> map = new LinkedHashMap<>();

        put(map, LoggingConstants.SYSTEM_NAME, systemName);
        put(map, LoggingConstants.NODE, nodeName);
        put(map, LoggingConstants.ENVIRONMENT, environment);

        Optional.ofNullable(includeMDC ? event.getMDCPropertyMap() : null)
                .ifPresent(m -> m.forEach((key, value) -> strictPut(map, key, value)));
        Optional.ofNullable(includeMessage ? event.getFormattedMessage() : null).ifPresent(msg -> put(map, LoggingConstants.MESSAGE, msg));
        Optional.ofNullable(includeException ? throwableProxyConverter.convert(event) : null)
                .filter(NON_EMPTY_STRING)
                .ifPresent(exceptionData -> put(map, LoggingConstants.EXCEPTION, exceptionData));

        if (!sparseLogging || (sparseLogging && event.getMDCPropertyMap().get(LoggingConstants.INITIAL_LOG_MARKER) != null)) {
            Optional.ofNullable(includeThreadName ? event.getThreadName() : null).ifPresent(threadName -> put(map, LoggingConstants.THREAD, threadName));
            Optional.ofNullable(includeLoggerName ? event.getLoggerName() : null).ifPresent(loggerName -> put(map, LoggingConstants.LOGGER, loggerName));
            Optional.ofNullable(includeRawMessage ? event.getFormattedMessage() : null).ifPresent(msg -> put(map, LoggingConstants.RAW_MESSAGE, msg));
            Optional.ofNullable(includeContextName ? event.getLoggerContextVO().getName() : null)
                    .ifPresent(contextName -> put(map, LoggingConstants.CONTEXT, contextName));
        }

        if (sparseLogging) {
            Map<String, String> oldMDC = MDC.getCopyOfContextMap();
            MDC.clear();
            if (oldMDC != null) {
                oldMDC.keySet()
                      .stream()
                      .filter((String k) -> standardMessageFields.contains(k) || retainedMDCFields.contains(k))
                      .forEach(k -> MDC.put(k, oldMDC.get(k)));
            }
        }

        MDC.remove(LoggingConstants.INITIAL_LOG_MARKER);

        return map;
    }

    private void strictPut(Map map, String key, String value) {
        Object valueToWrite = value;
        if (fieldsOf(xmlToJsonFields).contains(key)) {
            valueToWrite = xmlAsDataStructure(value);
        } else if (fieldsOf(jsonFields).contains(key)) {
            valueToWrite = jsonAsDataStructure(value);
        }

        put(map, key, valueToWrite);
    }

    protected void put(Map map, String key, Object value) {
        if (standardMessageFields.contains(key)) {
            map.put(key, value);
        } else {
            if (!map.containsKey(LoggingConstants.EXT)) {
                map.put(LoggingConstants.EXT, new LinkedHashMap<String, Object>());
            }
            ((Map) map.get(LoggingConstants.EXT)).put(key, value);
        }
    }
}