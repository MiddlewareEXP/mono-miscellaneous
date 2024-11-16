package com.mono.miscellaneous.common.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class LoggingConfig implements WebMvcConfigurer {

//    @Value("${custom.logging.file.errorLog}")
    private static String ERROR_LOG_FILE_PATH = "/opt/serviceLogs/monolithic/error.log";
//    @Value("${custom.logging.file.responseLog}")
    private static String RESPONSE_LOG_FILE_PATH = "/opt/serviceLogs/monolithic/response.log";

    // Configure error log appender
    @Bean
    public Appender<ILoggingEvent> errorFileAppender() throws IOException {
        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(loggerContext());
        appender.setFile(ERROR_LOG_FILE_PATH);
        appender.setEncoder(errorPatternLayoutEncoder());
        appender.start();
        return appender;
    }

    // Configure response log appender
    @Bean
    public Appender<ILoggingEvent> responseFileAppender() throws IOException {
        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(loggerContext());
        appender.setFile(RESPONSE_LOG_FILE_PATH);
        appender.setEncoder(responsePatternLayoutEncoder());
        appender.start();
        return appender;
    }

    // Configure error logger
    @Bean
    public ch.qos.logback.classic.Logger errorLogger() throws IOException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
        logger.addAppender(errorFileAppender());
        return logger;
    }

    // Configure response logger
    @Bean
    public ch.qos.logback.classic.Logger responseLogger() throws IOException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.mono.miscellaneous");
        logger.setLevel(Level.INFO);
        logger.addAppender(responseFileAppender());
        return logger;
    }

    // Configure pattern layout encoder for error logs
    @Bean
    public Encoder<ILoggingEvent> errorPatternLayoutEncoder() {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n");
        encoder.setContext(loggerContext());
        encoder.start();
        return encoder;
    }

    // Configure pattern layout encoder for response logs
    @Bean
    public Encoder<ILoggingEvent> responsePatternLayoutEncoder() {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n");
        encoder.setContext(loggerContext());
        encoder.start();
        return encoder;
    }

    // Create logger context
    private LoggerContext loggerContext() {
        LoggerContext context = new LoggerContext();
        context.setName("CustomLoggerContext");
        return context;
    }
}
