package com.mono.miscellaneous.controller;

import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RealtimeWeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/miscellaneous")
public class MainController {
    private RealtimeWeatherService realtimeWeatherService;

    public MainController(RealtimeWeatherService realtimeWeatherService) {
        this.realtimeWeatherService = realtimeWeatherService;
    }

    @GetMapping("/checkWeather")
    public RealtimeWeatherResponse getWeather(@RequestParam("lat") float lat, @RequestParam("lon") float lon) {
        return realtimeWeatherService.getRealTimeWeather(lat, lon);
    }
}
