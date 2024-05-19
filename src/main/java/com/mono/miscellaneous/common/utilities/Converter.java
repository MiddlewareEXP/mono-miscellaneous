package com.mono.miscellaneous.common.utilities;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class Converter {
    public static LocalTime getLocalTime(String timestampStr){
        long unixTimestamp = Long.parseLong(timestampStr);
        return LocalTime.ofSecondOfDay(unixTimestamp);
    }
}
