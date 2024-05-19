package com.mono.miscellaneous.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeWeatherResponse {
    private Location location;
    private Current current;
    private String responseCode;
    private String responseMsg;
    private String correlationId;
}
