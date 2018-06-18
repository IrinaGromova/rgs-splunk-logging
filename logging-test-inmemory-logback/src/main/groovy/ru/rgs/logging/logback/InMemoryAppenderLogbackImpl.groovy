/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited. 
 */

package ru.rgs.logging.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Layout
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import lombok.Setter
import ru.rgs.logging.core.CanonicalLogEvent
import ru.rgs.logging.core.InMemoryAppender

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Nesterenko Maxim (maksim_nesterenko@rgs.ru)
 (c) RGS Group, http://www.rgs.ru
 Created on 3/1/17
 */
@Slf4j
@CompileStatic
class InMemoryAppenderLogbackImpl extends AppenderBase<ILoggingEvent> implements InMemoryAppender {

    private static final Map<String, List> eventListsMap = new ConcurrentHashMap<>()

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private void init() {
        if (initialized.compareAndSet(false, true)) {
            eventListsMap.putIfAbsent(name, Collections.synchronizedList(new ArrayList<>()))
        }
    }

    private List events() {
        eventListsMap.get(name)
    }

    @Setter
    protected Layout<ILoggingEvent> layout

    @Override
    void clearEvents() {
        log.info "$this - clearEvents()"
        eventListsMap.put(name, Collections.synchronizedList(new ArrayList<CanonicalLogEvent>()))
    }

    void addEvent(CanonicalLogEvent event) {
        events().add(event)
    }

    @Override
    List<CanonicalLogEvent> getEvents() {
        init()
        // return a copy of current state, hence new ArrayList()
        def list = Collections.unmodifiableList(new ArrayList<CanonicalLogEvent>(events()))
        log.info "$this - getEvents(), returning ${list.size()} events"
        return list

    }

    @Override
    public void append(ILoggingEvent event) {
        init()
        // also log to file system for debugging
        log.info "$this - appending $event"

        def jsonSlurper = new JsonSlurper()
        CanonicalLogEvent canonicalLogEvent = jsonSlurper.parseText(layout != null ? layout.doLayout(event) : event.toString()) as CanonicalLogEvent
        // cannot validate canonical event here as it will be simply not logged in case of validation failure
        addEvent(canonicalLogEvent)

        log.info "$this - events in list now: ${events.size()}"
    }
}
