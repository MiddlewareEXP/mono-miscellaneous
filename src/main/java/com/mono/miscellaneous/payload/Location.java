package com.mono.miscellaneous.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String name;
    private String region;
    private String country;
    private float lat;
    private float lon;
    private String tz_id;
    private LocalTime localtime_epoch;
    private String localtime;
}
