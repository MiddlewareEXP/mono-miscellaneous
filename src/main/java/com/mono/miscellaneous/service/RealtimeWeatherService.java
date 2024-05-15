package com.mono.miscellaneous.service;

import com.mono.miscellaneous.payload.RealtimeWeatherResponse;

import javax.servlet.http.HttpServletRequest;

public interface RealtimeWeatherService {
    RealtimeWeatherResponse getRealTimeWeather(float lat, float lon);
}
