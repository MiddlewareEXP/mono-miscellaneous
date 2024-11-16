package com.mono.miscellaneous.service;

import com.mono.miscellaneous.payload.IpLookupRequest;
import com.mono.miscellaneous.payload.IpLookupResponse;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;

public interface RapidApiService {
    RealtimeWeatherResponse getRealTimeWeather(float lat, float lon, String correlationId);
    IpLookupResponse getIplookup(IpLookupRequest ipLookupRequest,String apiKey,String apiHost,String correlationId);
}
