/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging;

/**
 * @author jihor (jihor@ya.ru)
 *         Created on 2017-02-07
 */
public final class LoggingConstants {
    // standard fields
    public static final String CORRELATION_ID = "CorrelationId";
    public static final String ACTION_ID = "ActionId";
    public static final String ACTION_NAME = "Action";
    public static final String BUSINESS_OPERATION_CODE = "BusinessOperationCode";
    public static final String BUSINESS_OPERATION_NAME = "BusinessOperationName";
    public static final String SYSTEM_NAME = "System";
    public static final String ENVIRONMENT = "Environment";
    public static final String NODE = "Node";

    // additional fields
    public static final String EXT = "ext";

    public static final String INITIAL_LOG_MARKER = "InitialLogMarker";
    public static final String CLIENT_SYSTEM = "ClientSystem";
    public static final String TRANSPORT = "Transport";
    public static final String DURATION = "Duration";
    public static final String METHOD = "Method";
    public static final String THREAD = "Thread";
    public static final String MESSAGE  = "Message";
    public static final String EXCEPTION  = "Exception";
    public static final String SYNTHETIC = "synthetic_";
    public static final String LOGGER = "Logger";
    public static final String RAW_MESSAGE = "RawMessage";
    public static final String CONTEXT = "Context";
    public static final String NO_CACHE = "NoCache";

    // business operations messages
    public static final String COMPLETED = "Completed";
    public static final String STARTED = "Started";
    public static final String FAILED = "Failed";

    // transport types
    public static final String TRANSPORT_SOAP = "SOAP";
    public static final String TRANSPORT_REST = "REST";

    // prohibit class instantiation
    private LoggingConstants() {}
}
