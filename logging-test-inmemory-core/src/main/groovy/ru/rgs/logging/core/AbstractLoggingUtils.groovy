/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core

import groovy.transform.CompileStatic
import ru.rgs.logging.LoggingConstants

import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * * (С) RGS Group, http://www.rgs.ru
 * @author Nikolay Minyashkin (nikolay_minyashkin@rgs.ru)
 * Created on 6/27/16
 */
@CompileStatic
abstract class AbstractLoggingUtils {

    abstract protected List<CanonicalLogEvent> events()

    abstract void clearEvents()

    long countOf(Predicate predicate) {
        return events().stream().filter(predicate).count()
    }

    Optional<CanonicalLogEvent> firstOf(Predicate predicate) {
        return events().stream().filter(predicate).findFirst()
    }

    List<CanonicalLogEvent> getAll(Predicate predicate) {
        return (List<CanonicalLogEvent>) events().stream().filter(predicate).collect(Collectors.toList())
    }

    Predicate<CanonicalLogEvent> operations() {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event ->
            event.Environment == getEnvironment() &&
            event.Node == getNode() &&
            event.System == getSystem()
        }
    }

    abstract String getEnvironment()
    abstract String getNode()
    abstract String getSystem()

    static Predicate<CanonicalLogEvent> withMessage(String message) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.Message == message }
    }

    static Predicate<CanonicalLogEvent> startsWithMessage(String message) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.Message.startsWith(message) }
    }

    static Predicate<CanonicalLogEvent> withSyntheticCorrelationId() {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.CorrelationId.startsWith(LoggingConstants.SYNTHETIC) }
    }

    static Predicate<CanonicalLogEvent> withTransport(String transport) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.ext[LoggingConstants.TRANSPORT] == transport }
    }

    static Predicate<CanonicalLogEvent> withCorrelationId(String correlationId) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.CorrelationId == correlationId }
    }

    static Predicate<CanonicalLogEvent> withActionId(String actionId) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.ActionId == actionId }
    }

    static Predicate<CanonicalLogEvent> withAction(String action) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.Action == action }
    }

    static Predicate<CanonicalLogEvent> withBusinessOperationCode(String businessOperationCode) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.BusinessOperationCode == businessOperationCode }
    }

    static Predicate<CanonicalLogEvent> withBusinessOperationName(String businessOperationName) {
        return (Predicate<CanonicalLogEvent>) {CanonicalLogEvent event -> event.BusinessOperationName == businessOperationName }
    }

    static Predicate<CanonicalLogEvent> withException(String exception) {
        return (Predicate<CanonicalLogEvent>) {
            CanonicalLogEvent event -> event.ext[LoggingConstants.EXCEPTION] && event.ext[LoggingConstants.EXCEPTION].contains(exception)
        }
    }

    static Predicate<CanonicalLogEvent> withThread(String threadName) {
        return (Predicate<CanonicalLogEvent>) {
            CanonicalLogEvent event -> event.ext[LoggingConstants.THREAD] && event.ext[LoggingConstants.THREAD].contains(threadName)
        }
    }

    static Predicate<CanonicalLogEvent> withProperty(String propertyName, String value) {
        return (Predicate<CanonicalLogEvent>) {
            CanonicalLogEvent event -> value == event.ext[propertyName]
        }
    }

    static Predicate<CanonicalLogEvent> withProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            return (Predicate<CanonicalLogEvent>) {
                CanonicalLogEvent event -> entry.getValue() == event.ext[entry.getKey()]
            }
        }
    }
}
