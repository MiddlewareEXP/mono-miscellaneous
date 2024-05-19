package com.mono.miscellaneous.common.utilities;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class ReflexionHelper {
    public String getDeclaredFieldValue(Object request, String fieldName) {
        try {
            Field f = request.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(request).toString();
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            return "";
        }
    }

    public boolean checkForEmptyFields(Object request) {
        for (Field f : request.getClass().getDeclaredFields()) {

            f.setAccessible(true);
            try {
                if (f.get(request) == null || f.get(request) == "") {
                    return true;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return true;
            }
        }

        for (Field f : request.getClass().getSuperclass().getDeclaredFields()) {

            f.setAccessible(true);
            try {
                if (f.get(request) == null || f.get(request) == "") {
                    return true;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return true;
            }
        }
        return false;
    }
}
