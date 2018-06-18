/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

import static ru.rgs.logging.LoggingConstants.CORRELATION_ID;
import static ru.rgs.logging.LoggingConstants.SYNTHETIC;

/**
 * Common operations for logging
 *
 * @author jihor (dmitriy_zhikharev@rgs.ru)
 *         Created on 2017-05-29
 */
@Slf4j
public class LoggingUtils {
    public static String ensureCorrelationId(String correlationId) {
        if (StringUtils.isBlank(correlationId) || correlationId.trim().toLowerCase().equals("null") ) {
            correlationId = SYNTHETIC + UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID, correlationId);
            log.warn("Correlation ID was not found, created synthetic value");
        }
        return correlationId.trim();
    }
}
