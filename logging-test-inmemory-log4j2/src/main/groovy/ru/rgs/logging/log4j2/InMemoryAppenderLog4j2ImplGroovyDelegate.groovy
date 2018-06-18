/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.log4j2

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import ru.rgs.logging.core.CanonicalLogEvent
import ru.rgs.logging.core.InMemoryAppender

import java.util.concurrent.ConcurrentHashMap

/**
 * @author jihor (jihor@ya.ru)
 *         Created on 2017-05-12
 */

@Slf4j
class InMemoryAppenderLog4j2ImplGroovyDelegate extends AbstractAppender implements InMemoryAppender {

    private static final Map<String, List> eventListsMap = new ConcurrentHashMap<>()

    protected InMemoryAppenderLog4j2ImplGroovyDelegate(String name,
                                         Layout<? extends Serializable> layout) {
        super(name, null, layout)
        start()
        eventListsMap.putIfAbsent(name, Collections.synchronizedList(new ArrayList<>()))
    }

    @Override
    void append(LogEvent event) {
        // also log to file system for debugging
        log.info "$this - appending $event"

        def jsonSlurper = new JsonSlurper()
        CanonicalLogEvent canonicalLogEvent = jsonSlurper.parseText(getLayout().toSerializable(event).toString()) as CanonicalLogEvent
        // cannot validate canonical event here as it will be simply not logged in case of validation failure
        events().add(canonicalLogEvent)

        log.info "$this - events in list now: ${events().size()}"
    }

    private List events() {
        eventListsMap.get(name)
    }

    @Override
    void clearEvents() {
        log.info "$this - clearEvents()"
        eventListsMap.put(name, Collections.synchronizedList(new ArrayList<CanonicalLogEvent>()))
    }

    @Override
    List<CanonicalLogEvent> getEvents() {
        // return a copy of current state, hence new ArrayList()
        def list = Collections.unmodifiableList(new ArrayList<CanonicalLogEvent>(events()))
        log.info "$this - getEvents(), returning ${list.size()} events"
        return list

    }
}