package com.mono.miscellaneous.controller;

import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.payload.ErrorObject;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RealtimeWeatherService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/miscellaneous")
public class MainController extends BaseMiscellaneousController{
    private RealtimeWeatherService realtimeWeatherService;

    public MainController(RealtimeWeatherService realtimeWeatherService) {
        this.realtimeWeatherService = realtimeWeatherService;
    }

    @GetMapping("/checkWeatherReport")
    public ResponseEntity getWeatherReport(
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon
    ){
        JSONObject reqObj = new JSONObject();
        reqObj.put("lat",lat);
        reqObj.put("lon",lon);

        logRequest(reqObj);
        ResponseEntity response;
        try{
            RealtimeWeatherResponse objResponse = realtimeWeatherService.getRealTimeWeather(lat, lon, serviceID);
            if (objResponse != null){
                response = ResponseEntity.ok(objResponse);
            }else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorObject(serviceID, CommonEnum.ResponseCode.NOT_FOUND.getCode(), CommonEnum.ResponseCode.NOT_FOUND.getMessage(),timeStamp));
            }
            logResponse(response.getBody());
            return response;
        }catch (Exception e){
            return handleException(e);
        }
    }
}
