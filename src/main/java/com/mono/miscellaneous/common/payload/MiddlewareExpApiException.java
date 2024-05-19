package com.mono.miscellaneous.common.payload;

import com.mono.miscellaneous.common.utilities.CommonEnum;

public class MiddlewareExpApiException extends Exception{
    private CommonEnum.ResponseCode errorCode;
    private String errorMessage;

    public MiddlewareExpApiException(CommonEnum.ResponseCode errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString()
    {
        return errorCode.getCode().concat(" >> ").concat(errorCode.getMessage()).concat(" >> ").concat(errorMessage);
    }

    public CommonEnum.ResponseCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
