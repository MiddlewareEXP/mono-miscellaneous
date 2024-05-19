package com.mono.miscellaneous.common.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse {
    private String channelTransactionId;
    private String serviceId;
    private String timeStamp;
    private String responseCode;
    private String responseMessage;
}
