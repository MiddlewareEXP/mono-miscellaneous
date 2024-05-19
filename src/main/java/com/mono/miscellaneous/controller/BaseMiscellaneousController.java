package com.mono.miscellaneous.controller;

import com.mono.miscellaneous.common.controller.BaseController;
import com.mono.miscellaneous.common.logger.FileLogger;
import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.payload.ErrorObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseMiscellaneousController extends BaseController {
    @Autowired
    public FileLogger fileLogger;


    protected void logRequest(Object request) {
        setClientOperationDetails();
        fileLogger.requestLogger(clientIP, operationName, request, serviceID, false);
    }

    protected void logResponse(Object response) {
        fileLogger.responseLogger(clientIP, operationName, response, serviceID, false);
    }

    protected ResponseEntity handleException(Exception ex) {
        ex.printStackTrace();
        ResponseEntity resp = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorObject(serviceID, CommonEnum.ResponseCode.CONN_ERROR.getCode(),ex.getMessage(),timeStamp));
        fileLogger.responseLogger(clientIP, operationName, resp, serviceID, true);
        return resp;
    }
}
