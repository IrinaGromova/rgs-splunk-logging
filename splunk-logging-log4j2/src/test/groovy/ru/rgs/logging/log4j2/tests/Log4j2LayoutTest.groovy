/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.log4j2.tests

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import ru.rgs.logging.log4j2.Log4j2LoggingUtils
import ru.rgs.utils.Resources
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Type

import static ru.rgs.logging.LoggingConstants.ACTION_ID
import static ru.rgs.logging.LoggingConstants.ACTION_NAME
import static ru.rgs.logging.LoggingConstants.BUSINESS_OPERATION_CODE
import static ru.rgs.logging.LoggingConstants.BUSINESS_OPERATION_NAME
import static ru.rgs.logging.LoggingConstants.CORRELATION_ID
import static ru.rgs.logging.LoggingConstants.ENVIRONMENT
import static ru.rgs.logging.LoggingConstants.NODE
import static ru.rgs.logging.core.AbstractLoggingUtils.withAction
import static ru.rgs.logging.core.AbstractLoggingUtils.withActionId
import static ru.rgs.logging.core.AbstractLoggingUtils.withBusinessOperationCode
import static ru.rgs.logging.core.AbstractLoggingUtils.withBusinessOperationName
import static ru.rgs.logging.core.AbstractLoggingUtils.withCorrelationId
import static ru.rgs.logging.core.AbstractLoggingUtils.withProperty

/**
 * @author jihor (jihor@ya.ru)
 * Created on 2017-05-12
 */
class Log4j2LayoutTest extends Specification {

    private final Logger log = LoggerFactory.getLogger(Log4j2LayoutTest)
    private final Log4j2LoggingUtils loggingUtils = new Log4j2LoggingUtils("self")
    private final String SomeOtherProperty = "SomeOtherProperty"
    private Gson gson

    @Before
    void beforeMethod() {
        MDC.clear()
        loggingUtils.clearEvents()
        gson = new GsonBuilder().registerTypeAdapter(BigDecimal.class, new JsonSerializer<BigDecimal>() {

            private boolean isIntegerValue(BigDecimal bd) {
                return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0
            }

            @Override
            JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
                if (isIntegerValue(src)) {

                    return new JsonPrimitive(src.toLong())
                } else {
                    return new JsonPrimitive(src)
                }
            }
        }).create()
    }

    @Unroll
    def "Test Log4J2 layout with #keyToCheck field <-- #fileNameWithDataToCheck"(String fileNameWithDataToCheck, String fileNameWithExpectedData, String keyToCheck) {
        setup:
        prepareMdc()

        def data_to_check = Resources.readFromClassPath(Log4j2LayoutTest.class, fileNameWithDataToCheck, null)
        def expected_result = Resources.readFromClassPath(Log4j2LayoutTest.class, fileNameWithExpectedData, null)

        assert data_to_check != null
        assert expected_result != null

        when:
        MDC.put(keyToCheck, data_to_check)
        // log event
        log.info("Completed")

        // check result
        then:
        assert loggingUtils.events().size() == 1
        assert loggingUtils.countOf(loggingUtils.operations()
                                            & withActionId(MDC.get(ACTION_ID))
                                            & withAction(MDC.get(ACTION_NAME))
                                            & withCorrelationId(MDC.get(CORRELATION_ID))
                                            & withBusinessOperationCode(MDC.get(BUSINESS_OPERATION_CODE))
                                            & withBusinessOperationName(MDC.get(BUSINESS_OPERATION_NAME))
                                            & withProperty(SomeOtherProperty, MDC.get(SomeOtherProperty))
            ) == 1L

        assert gson.toJson(loggingUtils.events()[0].ext.get(keyToCheck)) == expected_result

        where:
        fileNameWithDataToCheck | fileNameWithExpectedData      | keyToCheck
        "xml1.xml"              | "xml1_expected.json"          | "XMLQueryBody"
        "json1.json"            | "json1_expected.json"         | "JsonQueryBody"
        "json_complex.json"     | "json_complex_expected.json"  | "JsonQueryBody"
        "json_complex2.json"    | "json_complex2_expected.json" | "JsonQueryBody"

    }

    private void prepareMdc() {
        [CORRELATION_ID, ACTION_ID].each { fillMdcRandomUUID(it) }
        [ACTION_NAME, BUSINESS_OPERATION_CODE, BUSINESS_OPERATION_NAME, ENVIRONMENT, NODE, SomeOtherProperty].each { fillMdcTestValue(it) }
    }

    private static void fillMdcTestValue(String key) {
        MDC.put(key, "test" + key)
    }

    private static void fillMdcRandomUUID(String key) {
        MDC.put(key, UUID.randomUUID().toString())
    }
}