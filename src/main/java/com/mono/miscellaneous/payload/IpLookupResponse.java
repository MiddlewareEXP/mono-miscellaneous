package com.mono.miscellaneous.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLookupResponse {
    private String ip;
    private String type;
    private String continentCode;
    private String continentName;
    private String countryCode;
    private String countryName;
    private String isEu;
    private String geonameId;
    private String city;
    private String region;
    private String lat;
    private String lon;
    private String tzId;
    private String localtimeEpoch;
    private String localtime;
    private String responseCode;
    private String responseMsg;
    private String correlationId;
}
