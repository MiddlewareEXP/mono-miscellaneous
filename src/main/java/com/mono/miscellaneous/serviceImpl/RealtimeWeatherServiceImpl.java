package com.mono.miscellaneous.serviceImpl;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mono.miscellaneous.payload.RealtimeWeatherResponse;
import com.mono.miscellaneous.service.RealtimeWeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class RealtimeWeatherServiceImpl implements RealtimeWeatherService {
    @Value("${rapid-api.host}")
    private String host;
    @Value("${rapid-api.key}")
    private String key;
    @Value("${rapid-api.keystorePassword}")
    private String keystorePassword;
    @Override
    public RealtimeWeatherResponse getRealTimeWeather(float lat, float lon) {

        Unirest.setTimeouts(0, 0);
        try {
            InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream("jks/keystore.jks");
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
            HttpResponse<String> response = Unirest.get("https://"+host+"/current.json?q="+lat+"%2C"+lon)
                    .header("X-RapidAPI-Key", key)
                    .header("X-RapidAPI-Host", host)
                    .asString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
