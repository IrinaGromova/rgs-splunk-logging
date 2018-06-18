/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.log4j2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.slf4j.MDC;
import ru.rgs.logging.LoggingConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static ru.rgs.logging.Converter.xmlAsDataStructure;
import static ru.rgs.logging.Converter.jsonAsDataStructure;
import static ru.rgs.logging.LoggingConstants.ACTION_ID;
import static ru.rgs.logging.LoggingConstants.ACTION_NAME;
import static ru.rgs.logging.LoggingConstants.BUSINESS_OPERATION_CODE;
import static ru.rgs.logging.LoggingConstants.BUSINESS_OPERATION_NAME;
import static ru.rgs.logging.LoggingConstants.CORRELATION_ID;
import static ru.rgs.logging.LoggingConstants.ENVIRONMENT;
import static ru.rgs.logging.LoggingConstants.EXCEPTION;
import static ru.rgs.logging.LoggingConstants.EXT;
import static ru.rgs.logging.LoggingConstants.INITIAL_LOG_MARKER;
import static ru.rgs.logging.LoggingConstants.MESSAGE;
import static ru.rgs.logging.LoggingConstants.NODE;
import static ru.rgs.logging.LoggingConstants.SYSTEM_NAME;
import static ru.rgs.logging.LoggingConstants.THREAD;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 2017-02-06
 */
@Plugin(name = "SplunkJsonLayout", category = "Core", elementType = "layout")
public class JsonLayout extends AbstractStringLayout {

    private final String systemName;
    private final String environment;
    private final String nodeName;

    private final boolean sparseLogging;
    // only these fields will be retained in MDC after logging if sparse logging is enabled
    private final List<String> retainedMDCFields;

    // only these fields will be written directly to event, all others will be written into extension section
    private final List<String> standardMessageFields;

    // these fields will be unwrapped from xml to json subtrees if present
    private final List<String> xmlToJsonFields;

    // these fields will be transformed to json subtrees if present
    private final List<String> jsonFields;
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();

    private final boolean includeThreadName;
    private final boolean includeMDC;
    private final boolean includeLoggerName;
    private final boolean includeMessage;
    private final boolean includeException;

    public JsonLayout(String systemName,
                      String environment,
                      String nodeName,
                      boolean sparseLogging,
                      List<String> retainedMDCFields,
                      List<String> standardMessageFields,
                      List<String> xmlToJsonFields,
                      List<String> jsonFields,
                      boolean includeThreadName,
                      boolean includeMDC,
                      boolean includeLoggerName,
                      boolean includeMessage,
                      boolean includeException) {
        super(Charset.forName("UTF-8"));
        this.systemName = systemName;
        this.environment = environment;
        this.nodeName = nodeName;
        this.sparseLogging = sparseLogging;
        this.retainedMDCFields = retainedMDCFields;
        this.standardMessageFields = standardMessageFields;
        this.xmlToJsonFields = xmlToJsonFields;
        this.jsonFields = jsonFields;
        this.includeThreadName = includeThreadName;
        this.includeMDC = includeMDC;
        this.includeLoggerName = includeLoggerName;
        this.includeMessage = includeMessage;
        this.includeException = includeException;
    }

    @SuppressWarnings("unused")
    @PluginFactory
    public static JsonLayout createLayout(@PluginAttribute(value = "systemName", defaultString = "undefined") String systemName,
                                          @PluginAttribute(value = "environment", defaultString = "undefined") String environment,
                                          @PluginAttribute(value = "nodeName", defaultString = "undefined") String nodeName,
                                          @PluginAttribute(value = "sparseLogging", defaultBoolean = true) boolean sparseLogging,
                                          @PluginAttribute(value = "retainedMDCFields") String retainedMDCFieldsAsString,
                                          @PluginAttribute(value = "standardMessageFields") String standardMessageFieldsAsString,
                                          @PluginAttribute(value = "xmlToJsonFields") String xmlToJsonFieldsAsString,
                                          @PluginAttribute(value = "jsonFields") String jsonFieldsAsString,
                                          @PluginAttribute(value = "includeThreadName", defaultBoolean = true) boolean includeThreadName,
                                          @PluginAttribute(value = "includeMDC", defaultBoolean = true) boolean includeMDC,
                                          @PluginAttribute(value = "includeLoggerName", defaultBoolean = true) boolean includeLoggerName,
                                          @PluginAttribute(value = "includeMessage", defaultBoolean = true) boolean includeMessage,
                                          @PluginAttribute(value = "includeException", defaultBoolean = true) boolean includeException) {

        List<String> retainedMDCFields = convertFieldsAsStringToList(retainedMDCFieldsAsString);
        retainedMDCFields.addAll(Arrays.asList(CORRELATION_ID, ACTION_ID));
        retainedMDCFields = Collections.unmodifiableList(retainedMDCFields);

        List<String> standardMessageFields = convertFieldsAsStringToList(standardMessageFieldsAsString);
        standardMessageFields.addAll(Arrays.asList(
                CORRELATION_ID,
                ACTION_ID,
                ACTION_NAME,
                BUSINESS_OPERATION_CODE,
                BUSINESS_OPERATION_NAME,
                SYSTEM_NAME,
                MESSAGE,
                ENVIRONMENT,
                NODE));
        standardMessageFields = Collections.unmodifiableList(standardMessageFields);

        List<String> xmlToJsonFields = convertFieldsAsStringToList(xmlToJsonFieldsAsString);
        xmlToJsonFields = Collections.unmodifiableList(xmlToJsonFields);

        List<String> jsonFields = Collections.unmodifiableList(convertFieldsAsStringToList(jsonFieldsAsString));

        return new JsonLayout(systemName,
                              environment,
                              nodeName,
                              sparseLogging,
                              retainedMDCFields,
                              standardMessageFields,
                              xmlToJsonFields,
                              jsonFields,
                              includeThreadName,
                              includeMDC,
                              includeLoggerName,
                              includeMessage,
                              includeException);
    }

    private static List<String> convertFieldsAsStringToList(String fieldsAsString) {
        List<String> resultList = new ArrayList<>();
        if (fieldsAsString != null) {
            for (String field : fieldsAsString.split(",")) {
                resultList.add(field.trim());
            }
        }
        return resultList;
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        try {
            return objectWriter.writeValueAsString(toMap(logEvent));
        } catch (IOException e) {
            String err = MessageFormat.format("Exception occurred on creating log message: {0}, stack: {1}",
                                              ExceptionUtils.getMessage(e),
                                              ExceptionUtils.getStackTrace(e));
            return "{"
                    + "\"" + SYSTEM_NAME + "\":\"" + systemName + "\","
                    + "\"" + NODE + "\":\"" + nodeName + "\","
                    + "\"" + ENVIRONMENT + "\":\"" + environment + "\","
                    + "\"" + MESSAGE + "\":\"" + err
                    + "\"}";
        }
    }

    private Map<String, Object> toMap(LogEvent event) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        appendEntryToMapWithNonNullValue(map, SYSTEM_NAME, systemName);
        appendEntryToMapWithNonNullValue(map, ENVIRONMENT, environment);
        appendEntryToMapWithNonNullValue(map, NODE, nodeName);

        if (includeMDC && event.getContextMap() != null) {
            for (Entry<String, String> entry : event.getContextMap().entrySet()) {
                String key = entry.getKey();
                Object value = xmlToJsonFields.contains(key) ?
                               xmlAsDataStructure(entry.getValue()) :
                               (jsonFields.contains(key) ? jsonAsDataStructure(entry.getValue()) : entry.getValue());
                appendEntryToMap(map, key, value);
            }
        }
        if (includeMessage) {
            appendEntryToMapWithNonNullValue(map, MESSAGE, event.getMessage().getFormattedMessage());
        }
        if (includeException && event.getThrown() != null) {
            appendEntryToMapWithNonNullValue(map,
                                             EXCEPTION,
                                             event.getThrown().getClass().getCanonicalName() + ": " + ExceptionUtils.getMessage(event.getThrown()) + "\n" +
                                                     ExceptionUtils.getStackTrace(event.getThrown()));
        }
        if (!sparseLogging || (sparseLogging && event.getContextMap() != null && event.getContextMap().get(INITIAL_LOG_MARKER) != null)) {
            if (includeThreadName) {
                appendEntryToMapWithNonNullValue(map, THREAD, event.getThreadName());
            }
            if (includeLoggerName) {
                appendEntryToMapWithNonNullValue(map, LoggingConstants.LOGGER, event.getLoggerName());
            }
        }
        if (sparseLogging) {
            cleanupMDC();
        }

        MDC.remove(INITIAL_LOG_MARKER);
        return map;
    }

    private void appendEntryToMapWithNonNullValue(Map<String, Object> map, String entryKey, Object entryValue) {
        if (entryValue != null) {
            appendEntryToMap(map, entryKey, entryValue);
        }
    }

    private void appendEntryToMap(Map<String, Object> map, String entryKey, Object entryValue) {
        if (standardMessageFields.contains(entryKey)) {
            map.put(entryKey, entryValue);
        } else {
            if (!map.containsKey(EXT)) {
                map.put(EXT, new LinkedHashMap<String, Object>());
            }
            ((Map<String, Object>) map.get(EXT)).put(entryKey, entryValue);
        }
    }

    private void cleanupMDC() {
        Map<String, String> oldMDC = MDC.getCopyOfContextMap();
        MDC.clear();
        if (oldMDC != null) {
            for (Entry<String, String> entry : oldMDC.entrySet()) {
                if (standardMessageFields.contains(entry.getKey()) || retainedMDCFields.contains(entry.getKey())) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}