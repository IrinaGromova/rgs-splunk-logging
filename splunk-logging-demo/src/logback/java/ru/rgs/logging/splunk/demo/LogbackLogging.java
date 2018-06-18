/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.splunk.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import ru.rgs.utils.Resources;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.rgs.logging.LoggingConstants.ACTION_ID;
import static ru.rgs.logging.LoggingConstants.ACTION_NAME;
import static ru.rgs.logging.LoggingConstants.CORRELATION_ID;

@Slf4j(topic = "businessOperationLogger")
public class LogbackLogging {

    private static final Map<String, String[]> CUSTOM_FIELDS = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>("xml1", new String[]{"xml1.xml", "<rgs><value>4325461411</value></rgs>"}),
            new SimpleEntry<>("json1", new String[]{"json1.json", "{\"rgs\":{\"CalcResponseValue\":1}}"}),
            new SimpleEntry<>("json2", new String[]{"json_complex.json", "{\"rgs\":{\"CalcResponseValue\":1}}"}),
            new SimpleEntry<>("json3", new String[]{"json_complex2.json", "{\"rgs\":{\"CalcResponseValue\":1}}"})
    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    public static void main(String[] args) throws InterruptedException {
        MDC.put(ACTION_NAME, "Logback logging test");
        MDC.put(CORRELATION_ID, "Some correlation id");
        MDC.put(ACTION_ID, "Some action id");
        CUSTOM_FIELDS.forEach((k, v) -> MDC.put(k, Resources.readFromClassPath(LogbackLogging.class, v[0], v[1])));

        log.error("LogbackLogging");
        TimeUnit.SECONDS.sleep(5L);
        System.exit(0);
    }
}