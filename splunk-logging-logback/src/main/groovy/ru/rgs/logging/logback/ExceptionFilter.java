package ru.rgs.logging.logback;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ExceptionFilter extends TurboFilter {
	private static final String TRACE_SIZE_MUST_BE_GREATER_0 = "Trace size must be greater 0";
	private static final String ALLOWED_REPETITION_MUST_BE_GREATER_0 = "Allowed repetition must be greater 0";
	private static final int DEFAULT_CACHE_SIZE = 20;
	private static final int DEFAULT_ALLOWED_REPETITIONS = 5;
	private static final int DEFAULT_TRACE_SIZE = 100;

	private int allowedRepetitions = DEFAULT_ALLOWED_REPETITIONS;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private int traceSize = DEFAULT_TRACE_SIZE;

	private LRUCache cache = new LRUCache(cacheSize);
	
	

	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
		// Message hasn't exception
		if ((t == null) || (t.getStackTrace() == null)) {
			return FilterReply.NEUTRAL;
		}
		StringBuilder buffer = getPlaneTrace(t);
		if (isTraceNotAllowed(buffer)) {
			clearStackTrace(t);
		}
		return FilterReply.NEUTRAL;
	}

	private void clearStackTrace(Throwable t) {
		StackTraceElement[] newTrace = new StackTraceElement[0];
		t.setStackTrace(newTrace);
	}

	private StringBuilder getPlaneTrace(Throwable t) {
		StringBuilder buffer = new StringBuilder();
		StackTraceElement[] trace = t.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			buffer.append(trace[i].toString());
			if (buffer.length() >= traceSize) {
				break;
			}
		}
		//trim
		if (buffer.length() > traceSize) {
			buffer.setLength(traceSize);
		}
		return buffer;
	}

	private boolean isTraceNotAllowed(StringBuilder trace) {
		int count = cache.addStackTrace(trace.toString());
		return count > allowedRepetitions;
	}

	public int getAllowedRepetitions() {
		return allowedRepetitions;
	}

	public void setAllowedRepetitions(int allowedRepetitions) {
		if (allowedRepetitions < 1) {
			throw new IllegalArgumentException(ALLOWED_REPETITION_MUST_BE_GREATER_0);
		}
		this.allowedRepetitions = allowedRepetitions;
	}

	public int getTraceSize() {
		return traceSize;
	}

	public void setTraceSize(int traceSize) {
		if (cacheSize < 1) {
			throw new IllegalArgumentException(TRACE_SIZE_MUST_BE_GREATER_0);
		}
		this.traceSize = traceSize;
	}

}
