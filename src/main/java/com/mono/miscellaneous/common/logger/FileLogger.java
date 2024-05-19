package com.mono.miscellaneous.common.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import com.mono.miscellaneous.common.utilities.CommonUtilities;
import com.mono.miscellaneous.serviceImpl.RealtimeWeatherServiceImpl;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Field;

@Repository
public class FileLogger {
    private static final Logger logger = LoggerFactory.getLogger(RealtimeWeatherServiceImpl.class);

    public void requestLogger(String clientIp, String operationName, Object myObject, String serviceId, boolean isError) {
        String tString = "";
        Object obj = new Object();
        tString = "{ ";
        for (Field f : myObject.getClass().getDeclaredFields()) {
            tString = tString + maskField(myObject, obj, f, false) + ", ";
        }
        for (Field f : myObject.getClass().getSuperclass().getDeclaredFields()) {
            tString = tString + maskField(myObject, obj, f, true) + ", ";
        }
        tString = tString.substring(0, tString.length() - 2);
        tString = tString + " }";

        if (isError) {
            logger.error("Sending request to upstream API - CorrelationId:  " + serviceId + ", Operation Name: " + operationName + ", Client IP: " + clientIp + ", Error: " + tString);//+ requestJson);
        } else {
            logger.info("Sending request to upstream API - CorrelationId:  " + serviceId + ", Operation Name: " + operationName + ", Client IP: " + clientIp + ", Request: " + tString);// + requestJson);
        }
    }

    public void responseLogger(String clientIp, String operationName, Object myObject, String serviceId, boolean isError) {
        String tString = "";
        Object obj = new Object();
        tString = "{ ";
        for (Field f : myObject.getClass().getDeclaredFields()) {
            tString = tString + maskField(myObject, obj, f, false) + ", ";
        }
        for (Field f : myObject.getClass().getSuperclass().getDeclaredFields()) {
            tString = tString + maskField(myObject, obj, f, true) + ", ";
        }
        tString = tString.substring(0, tString.length() - 2);
        tString = tString + " }";

        if (isError) {
            logger.error("Received response from upstream API - CorrelationId:  " + serviceId + ", Operation Name: " + operationName + ", Client IP: " + clientIp + ", Response Error: " + tString);
        } else {
            logger.info("Received response from upstream API - CorrelationId:  " + serviceId + ", Operation Name: " + operationName + ", Client IP: " + clientIp + ", Response: " + tString);
        }
    }

    private String maskField(Object myObject, Object obj, Field f, boolean superClass){
        String tString = "";
        String password = "";
        String PIN = "";
        String smsText = "";

        f.setAccessible(true);
        try {
            if (f.getName().trim().equals("pin")) {
                if(f.get(myObject) != null) {
                    password = f.get(myObject).toString().trim();
                    password = this.maskPassword(password);
                    tString = tString + f.getName() + ":" + password + "  ";
                }
            } else if (f.getName().trim().equals("newPin")) {
                if(f.get(myObject) != null) {
                    PIN = f.get(myObject).toString().trim();
                    PIN = this.maskPassword(PIN);
                    tString = tString + "******" + ":" + PIN + "*****  ";
                }
            }  else if (f.getName().trim().equals("oldPin")) {
                if(f.get(myObject) != null) {
                    PIN = f.get(myObject).toString().trim();
                    PIN = this.maskPassword(PIN);
                    tString = tString + "******" + ":" + PIN + "*****,  ";
                }
            } else if (f.getName().trim().equals("message()")) {
                if(f.get(myObject) != null) {
                    smsText = f.get(myObject).toString().trim();
                    smsText = smsText.replaceAll("\\d", "");
                    tString = tString + "******" + ":" + smsText + "*****  ";
                }
            } else {
                tString = tString + f.getName() + ":" + f.get(myObject) + "  ";
            }



            return tString;
        } catch (IllegalArgumentException | IllegalAccessException e) { //| NoSuchFieldException e) {
            logger.error("Request log db error: " + e.getMessage());
            return "";
        }
    }
    public String maskPassword(String password) {
        return CommonUtilities.maskNumber(password, 0, password.length(), '*');
    }
}
