package com.mono.miscellaneous.serviceImpl;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RealtimeWeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.security.KeyStore;
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
    public RealtimeWeatherResponse getRealTimeWeather(float lat, float lon) {
        Unirest.setTimeouts(0,0);
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
            logger.info("Sending request to downstream API - URL: {}, Method: GET, Latitude: {}, Longitude: {}",
                    "https://" + host + "/current.json?q=" + lat + "%2C" + lon, lat, lon);

            HttpResponse<String> response = Unirest.get("https://" + host + "/current.json?q=" + lat + "%2C" + lon)
                    .header("X-RapidAPI-Key", key)
                    .header("X-RapidAPI-Host", host)
                    .asString();

            // Log response
            logger.info("Received response from downstream API - Status: {}, Body: {}",
                    response.getStatus(), response.getBody());
        } catch (Exception e) {
            logger.error("Error occurred while calling downstream API: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return null;
    }
}
