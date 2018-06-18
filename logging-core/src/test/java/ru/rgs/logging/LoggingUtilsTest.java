/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class LoggingUtilsTest {

    @Before
    public void beforeMethod() {
        MDC.clear();
    }

    @Test
    public void ensureCorrelationIdTest() {
        checkIfReturnsSyntheticUUID(LoggingUtils.ensureCorrelationId("null"));
        checkIfReturnsSyntheticUUID(LoggingUtils.ensureCorrelationId("   null "));
        checkIfReturnsSyntheticUUID(LoggingUtils.ensureCorrelationId(" NULL "));
        checkIfReturnsSyntheticUUID(LoggingUtils.ensureCorrelationId(" "));
        checkIfReturnsSyntheticUUID(LoggingUtils.ensureCorrelationId(null));
        Assert.assertEquals(LoggingUtils.ensureCorrelationId(" testme "), "testme");
    }

    private void checkIfReturnsSyntheticUUID(String value){
        Assert.assertTrue(value.startsWith(SYNTHETIC));
        // if the string is not a valid UUID, UUID.fromString() will throw an exception
        Assert.assertThat(UUID.fromString(value.split(SYNTHETIC)[1]), Is.isA(UUID.class));
    }

}
