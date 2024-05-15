package com.mono.miscellaneous.service;

import com.mono.miscellaneous.payload.RealtimeWeatherResponse;

public interface RealtimeWeatherService {
    RealtimeWeatherResponse getRealTimeWeather(float lat, float lon);
}
