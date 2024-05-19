package com.mono.miscellaneous.payload;

import lombok.ToString;

@ToString
public class ErrorObject {
    public String correlationId;
    public String responseCode;
    public String responseMessage;
    public String timeStamp;

    public String getError() {
        return responseMessage;
    }

    public void setError(String error) {
        this.responseMessage = responseMessage;
    }

    public ErrorObject(
            String correlationId,
            String responseCode,
            String responseMessage,
            String timeStamp
    ) {
        this.correlationId = correlationId;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.timeStamp = timeStamp;
    }


}
