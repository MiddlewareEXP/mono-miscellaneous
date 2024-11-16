package com.mono.miscellaneous.controller;

import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.payload.ErrorObject;
import com.mono.miscellaneous.payload.IpLookupRequest;
import com.mono.miscellaneous.payload.IpLookupResponse;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RapidApiService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/miscellaneous/v1")
public class MainController{
    @Autowired
    private RapidApiService rapidApiService;

    private final SimpleDateFormat ft3 = new SimpleDateFormat("yyDDDHHmm");
    private final SimpleDateFormat ft4 = new SimpleDateFormat("yyyyMMddHHmmss");

    private static AtomicInteger sequenceNumber = new AtomicInteger(0);

    protected String getServiceID() {
        Date myDate = new Date();
        String sequenceString = String.format("%04d", sequenceNumber.getAndIncrement() % 1000);
        return ft3.format(myDate) + sequenceString;
    }

    protected String getTimeStamp() {
        Date myDate = new Date();
        return ft4.format(myDate);
    }

    @GetMapping("/checkHealth")
    public String healthCheck(){
        return "Responding !!";
    }

    @GetMapping("/checkWeatherReport")
    public ResponseEntity getWeatherReport(
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon
    ){
        JSONObject reqObj = new JSONObject();
        reqObj.put("lat",lat);
        reqObj.put("lon",lon);

        ResponseEntity response;
        try{
            RealtimeWeatherResponse objResponse = rapidApiService.getRealTimeWeather(lat, lon, getServiceID());
            if (objResponse != null){
                response = ResponseEntity.ok(objResponse);
            }else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorObject(getServiceID(), CommonEnum.ResponseCode.NOT_FOUND.getCode(), CommonEnum.ResponseCode.NOT_FOUND.getMessage(),getTimeStamp()));
            }
            return response;
        }catch (Exception e){
            response = ResponseEntity.internalServerError().body(e.getMessage());
            return response;
        }
    }

    @PostMapping("/getIpLookUp")
    public ResponseEntity getIpLookUp(
            @RequestBody IpLookupRequest ipLookupRequest,
            @RequestHeader("apiKey") String apiKey,
            @RequestHeader("apiHost") String apiHost
    )
    {
        JSONObject reqObj = new JSONObject();
        reqObj.put("ip",ipLookupRequest.getIp());
        reqObj.put("apiKey",apiKey);
        reqObj.put("apiHost",apiHost);

        ResponseEntity response;
        try{
            IpLookupResponse objResponse = rapidApiService.getIplookup(ipLookupRequest,apiKey,apiHost,getServiceID());
            if (objResponse != null){
                response = ResponseEntity.ok(objResponse);
            }else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorObject(getServiceID(), CommonEnum.ResponseCode.NOT_FOUND.getCode(), CommonEnum.ResponseCode.NOT_FOUND.getMessage(),getTimeStamp()));
            };
            return response;
        }catch (Exception e){
            response = ResponseEntity.internalServerError().body(e.getMessage());
            return response;
        }
    }
}
