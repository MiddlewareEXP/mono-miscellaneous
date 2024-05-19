package com.mono.miscellaneous.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.common.utilities.Converter;
import com.mono.miscellaneous.payload.Condition;
import com.mono.miscellaneous.payload.Current;
import com.mono.miscellaneous.payload.Location;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RealtimeWeatherService;
import jakarta.servlet.http.HttpServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RealtimeWeatherServiceImpl implements RealtimeWeatherService {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeWeatherServiceImpl.class);

    @Value("${rapid-api.host}")
    private String host;
    @Value("${rapid-api.key}")
    private String key;
    @Value("${rapid-api.keystorePassword}")
    private String keystorePassword;

    @Override
    public RealtimeWeatherResponse getRealTimeWeather(float lat, float lon, String correlationId) {
        Unirest.setTimeouts(0, 0);
        RealtimeWeatherResponse res;
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

            res = getRealtimeWeatherResponse(response);
            res.setCorrelationId(correlationId);
            res.setResponseCode(CommonEnum.ResponseCode.REQUEST_SUCCESS.getCode());
            res.setResponseMsg(CommonEnum.ResponseCode.REQUEST_SUCCESS.getMessage());

        } catch (Exception e) {
            logger.error("Error occurred while calling downstream API: {}", e.getMessage());
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
}
