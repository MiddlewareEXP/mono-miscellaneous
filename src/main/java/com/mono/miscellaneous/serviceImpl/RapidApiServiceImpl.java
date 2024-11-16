package com.mono.miscellaneous.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.payload.*;
import com.mono.miscellaneous.service.RapidApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RapidApiServiceImpl implements RapidApiService {

    private static final Logger logger = LoggerFactory.getLogger(RapidApiServiceImpl.class);

    @Value("${rapid-api.host}")
    private String host;
    @Value("${rapid-api.key}")
    private String key;
    @Value("${rapid-api.keystorePassword}")
    private String keystorePassword;

    @Override
    public RealtimeWeatherResponse getRealTimeWeather(float lat, float lon, String correlationId) {
//        Unirest.setTimeouts(0, 0);
        RealtimeWeatherResponse res = new RealtimeWeatherResponse();
        try {
            InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream("cert/keystore.jks");
            KeyStore keystore = KeyStore.getInstance("jks");
            keystore.load(keystoreStream, keystorePassword.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, keystorePassword.toCharArray());

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Configure Unirest to use custom SSL context
            Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom().setSSLContext(sslContext).build());

            // Get correlation ID from request headers
            //String correlationId = request.getHeader("correlationId");

            // Log request
            logger.info("Sending request to downstream API - URL: {}, Method: GET, Latitude: {}, Longitude: {}, CorrelationId: {}",
                    "https://" + host + "/current.json?q=" + lat + "%2C" + lon, lat, lon, correlationId);

            HttpResponse<String> response = Unirest.get("https://" + host + "/current.json?q=" + lat + "%2C" + lon)
                    .header("X-RapidAPI-Key", key)
                    .header("X-RapidAPI-Host", host)
                    .asString();

            // Log response
            logger.info("Received response from downstream API - Status: {}, Body: {}, , CorrelationId: {}",
                    response.getStatus(), response.getBody(), correlationId);

            if (response.getStatus() == 200){
                res = getRealtimeWeatherResponse(response);
                res.setCorrelationId(correlationId);
                res.setResponseCode(CommonEnum.ResponseCode.REQUEST_SUCCESS.getCode());
                res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_SUCCESS.getMessage());
            }else {
                res.setCorrelationId(correlationId);
                res.setResponseCode(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getCode());
                res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error occurred while calling downstream API: {}", e.getMessage());
            res.setCorrelationId(correlationId);
            res.setResponseCode(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getCode());
            res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getMessage());
            throw new RuntimeException(e);
        }

        return res;
    }

    @Override
    public IpLookupResponse getIplookup(IpLookupRequest ipLookupRequest,String apiKey,String apiHost,String correlationId) {
//        Unirest.setTimeouts(0, 0);
        IpLookupResponse res = new IpLookupResponse();
        try{
            InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream("cert/keystore.jks");
            KeyStore keystore = KeyStore.getInstance("jks");
            keystore.load(keystoreStream, keystorePassword.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, keystorePassword.toCharArray());

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Configure Unirest to use custom SSL context
            Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom().setSSLContext(sslContext).build());

            // Log request
            logger.info("Sending request to downstream API - URL: {}, Method: GET, Ip: {}, CorrelationId: {}",
                    "https://" + host + "/ip.json?q=", ipLookupRequest.getIp(), correlationId);

            HttpResponse<String> response = Unirest.get("https://"+host+"/ip.json?q="+ipLookupRequest.getIp())
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .asString();

            // Log response
            logger.info("Received response from downstream API - Status: {}, Body: {}, , CorrelationId: {}",
                    response.getStatus(), response.getBody(), correlationId);

            if (response.getStatus() == 200){
                res = getIpLookupResponse(response);
                res.setCorrelationId(correlationId);
                res.setResponseCode(CommonEnum.ResponseCode.REQUEST_SUCCESS.getCode());
                res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_SUCCESS.getMessage());
            }else {
                res.setCorrelationId(correlationId);
                res.setResponseCode(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getCode());
                res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getMessage());
            }

        }catch (Exception e) {
            logger.error("Error occurred while calling downstream API: {}", e.getMessage());
            res.setCorrelationId(correlationId);
            res.setResponseCode(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getCode());
            res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS.getMessage());
            throw new RuntimeException(e);
        }

        return res;
    }

    private RealtimeWeatherResponse getRealtimeWeatherResponse(HttpResponse<String> response){
        Condition condition = new Condition();
        Current current = new Current();
        Location location = new Location();
        RealtimeWeatherResponse realtimeWeatherResponse = new RealtimeWeatherResponse();

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode locationNode = rootNode.get("location");
            JsonNode currentNode = rootNode.get("current");
            JsonNode conditionNode = currentNode.get("condition");

            location.setName(locationNode.get("name").asText());
            location.setRegion(locationNode.get("region").asText());
            location.setCountry(locationNode.get("country").asText());
            location.setLat(locationNode.get("lat").floatValue());
            location.setLon(locationNode.get("lon").floatValue());
            location.setTz_id(locationNode.get("tz_id").asText());
            location.setLocaltime_epoch(Long.parseLong(locationNode.get("localtime_epoch").asText()));
            location.setLocaltime(locationNode.get("localtime").asText());
            current.setLast_updated_epoch(Long.parseLong(currentNode.get("last_updated_epoch").asText()));
            current.setLast_updated(currentNode.get("last_updated").asText());
            current.setTemp_c(currentNode.get("temp_c").floatValue());
            current.setTemp_f(currentNode.get("temp_f").floatValue());
            current.setIs_day(currentNode.get("is_day").asInt());
            condition.setText(conditionNode.get("text").asText());
            condition.setIcon(conditionNode.get("icon").asText());
            condition.setCode(conditionNode.get("code").asInt());
            current.setCondition(condition);
            current.setWind_mph(currentNode.get("wind_mph").floatValue());
            current.setWind_kph(currentNode.get("wind_kph").floatValue());
            current.setWind_degree(currentNode.get("wind_degree").asInt());
            current.setWind_dir(currentNode.get("wind_dir").asText());
            current.setPressure_mb(currentNode.get("pressure_mb").floatValue());
            current.setPressure_in(currentNode.get("pressure_in").floatValue());
            current.setPrecip_mm(currentNode.get("precip_mm").floatValue());
            current.setPrecip_in(currentNode.get("precip_in").floatValue());
            current.setHumidity(currentNode.get("humidity").asInt());
            current.setCloud(currentNode.get("cloud").asInt());
            current.setFeelslike_c(currentNode.get("feelslike_c").floatValue());
            current.setFeelslike_f(currentNode.get("feelslike_f").floatValue());
            current.setVis_km(currentNode.get("vis_km").floatValue());
            current.setVis_miles(currentNode.get("vis_miles").floatValue());
            current.setUv(currentNode.get("uv").floatValue());
            current.setGust_mph(currentNode.get("gust_mph").floatValue());
            current.setGust_kph(currentNode.get("gust_kph").floatValue());
            realtimeWeatherResponse.setLocation(location);
            realtimeWeatherResponse.setCurrent(current);

        } catch (JsonMappingException e) {
            logger.error("Error occurred while calling downstream API getRealtimeWeatherResponse mapping: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while calling downstream API getRealtimeWeatherResponse mapping: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return realtimeWeatherResponse;
    }

    private IpLookupResponse getIpLookupResponse(HttpResponse<String> response){
        IpLookupResponse ipLookupResponse = new IpLookupResponse();
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            ipLookupResponse.setIp(rootNode.get("ip").asText());
            ipLookupResponse.setType(rootNode.get("type").asText());
            ipLookupResponse.setContinentCode(rootNode.get("continent_code").asText());
            ipLookupResponse.setContinentName(rootNode.get("continent_name").asText());
            ipLookupResponse.setCountryCode(rootNode.get("country_code").asText());
            ipLookupResponse.setCountryName(rootNode.get("country_name").asText());
            ipLookupResponse.setIsEu(String.valueOf(rootNode.get("is_eu").isBoolean()));
            ipLookupResponse.setGeonameId(String.valueOf(rootNode.get("geoname_id").asInt()));
            ipLookupResponse.setCity(rootNode.get("city").asText());
            ipLookupResponse.setRegion(rootNode.get("region").asText());
            ipLookupResponse.setLat(String.valueOf(rootNode.get("lat").floatValue()));
            ipLookupResponse.setLon(String.valueOf(rootNode.get("lon").floatValue()));
            ipLookupResponse.setTzId(rootNode.get("tz_id").asText());
            ipLookupResponse.setLocaltimeEpoch(rootNode.get("localtime_epoch").asText());
            ipLookupResponse.setLocaltime(rootNode.get("localtime").asText());

        } catch (JsonMappingException e) {
            logger.error("Error occurred while calling downstream API getIpLookupResponse mapping: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while calling downstream API getIpLookupResponse mapping: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return ipLookupResponse;
    }
}
