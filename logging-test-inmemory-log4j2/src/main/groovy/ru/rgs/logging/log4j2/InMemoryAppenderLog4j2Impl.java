/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.log4j2;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import ru.rgs.logging.core.CanonicalLogEvent;
import ru.rgs.logging.core.InMemoryAppender;

import java.io.Serializable;
import java.util.List;

/**
 * @author jihor (jihor@ya.ru)
 *         Created on 2017-05-12
 *         Groovy has its own annotation processor and, since, it's incompatible with other annotation processors,
 *         there is no possibility annotations used by annotation processors other than Groovy in .groovy files.
 *
 *         \@Plugin annotation is processed by log4j-core annotation processor and therefore can't work in .groovy files
 *         ( if used, META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat/Log4j2Plugins.dat will not be generated
 *         and the plugin will not work).
 *
 *         A workaround is to separate sources to Java and Groovy, and delegate behavior to Groovy implementation.
 *
 */

@Plugin(name = "InMemoryAppender", category = "Core", elementType = "appender", printObject = true)
public class InMemoryAppenderLog4j2Impl implements Appender, InMemoryAppender {
    private InMemoryAppenderLog4j2Impl(String name, Layout<? extends Serializable> layout) {
        delegate = new InMemoryAppenderLog4j2ImplGroovyDelegate(name, layout);
    }

    @PluginBuilderFactory
    static <B extends Builder<B>> B appenderBuilder() {
        return new Builder<B>().asBuilder();
    }

    static class Builder<B extends Builder<B>>
            extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<InMemoryAppenderLog4j2Impl> {
        @Override
        public InMemoryAppenderLog4j2Impl build() {
            Layout<? extends Serializable> layout = this.getOrCreateLayout();
            return new InMemoryAppenderLog4j2Impl(this.getName(), layout);
        }
    }

    private InMemoryAppenderLog4j2ImplGroovyDelegate delegate;

    // Using Lombok's @Delegatate led to a compilation error, so all methods had to be delegated manually

    @Override
    public void append(LogEvent logEvent) {
        delegate.append(logEvent);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return delegate.getLayout();
    }

    @Override
    public boolean ignoreExceptions() {
        return delegate.ignoreExceptions();
    }

    @Override
    public ErrorHandler getHandler() {
        return delegate.getHandler();
    }

    @Override
    public void setHandler(ErrorHandler errorHandler) {
        delegate.setHandler(errorHandler);
    }

    @Override
    public State getState() {
        return delegate.getState();
    }

    @Override
    public void initialize() {
        delegate.initialize();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public boolean isStopped() {
        return delegate.isStopped();
    }

    @Override
    public void clearEvents() {
        delegate.clearEvents();
    }

    @Override
    public List<CanonicalLogEvent> getEvents() {
        return delegate.getEvents();
    }
}
