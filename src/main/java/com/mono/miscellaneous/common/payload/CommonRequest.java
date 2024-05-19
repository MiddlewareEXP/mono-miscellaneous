package com.mono.miscellaneous.common.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRequest {
    private String channelName;
    private String channelSecret;
    private String channelTransactionId;
}
