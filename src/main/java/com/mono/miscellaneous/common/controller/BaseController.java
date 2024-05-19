package com.mono.miscellaneous.common.controller;

import com.mono.miscellaneous.common.payload.CommonRequest;
import com.mono.miscellaneous.common.payload.CommonResponse;
import com.mono.miscellaneous.common.payload.MiddlewareExpApiException;
import com.mono.miscellaneous.common.utilities.CommonEnum;
import com.mono.miscellaneous.common.utilities.ReflexionHelper;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseController {
    @Value("{X-B3-TraceId:-}")
    private String traceId;
    private HttpServletRequest httpServletRequest;
    private ReflexionHelper reflexionHelper = new ReflexionHelper();

    protected String clientIP;
    protected String operationName;
    protected String serviceID;
    protected String timeStamp;

    private SimpleDateFormat ft3 = new SimpleDateFormat( "yyDDDHHmm"); //"ddmmss");
    private SimpleDateFormat ft4 = new SimpleDateFormat("yyyyMMddHHmmss");

    private static int sequenceNumber;

//    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    protected String getServiceID() {
        Date myDate = new Date();
        String sequenceString = "0000" + String.valueOf(sequenceNumber);
        if(sequenceNumber < 1000)
            sequenceNumber++;
        else
            sequenceNumber = 0;
        String serviceID = ft3.format(myDate).toString() + sequenceString.substring(sequenceString.length() - 3, sequenceString.length()); //+ (new Random().nextInt(10)); //+ myRandom;
        return serviceID;

    }

    protected String getTimeStamp() {
        Date myDate = new Date();
        return ft4.format(myDate).toString();
    }

    private String getClientIP() {
        String remoteAddr = "";

        if (httpServletRequest != null) {
            remoteAddr = httpServletRequest.getRemoteAddr();
        }

        return remoteAddr;
    }

    private String getOperationName() {
        String operationName = "";

        try {
            operationName = httpServletRequest.getRequestURI() + "/" + httpServletRequest.getMethod();
        } catch (Exception ex) {
            operationName = "";
        }

        return operationName;
    }

    protected void setClientOperationDetails() {
        clientIP = getClientIP();
        operationName = getOperationName();
        serviceID = getServiceID();
        timeStamp = getTimeStamp();
    }

    protected ResponseEntity logAndGetAPIResponse(CommonRequest objRequest, CommonResponse objResponse, CommonEnum.ResponseCode responseCode)
    {
        return getAPIResponse(objRequest, objResponse, responseCode);
    }

    protected ResponseEntity logAndGetAPIResponse(CommonRequest objRequest, CommonResponse objResponse)
    {
        return getAPIResponse(objRequest, objResponse);
    }

    protected ResponseEntity getAPIResponse(CommonRequest objRequest, CommonResponse objResponse, CommonEnum.ResponseCode responseCode)
    {
        prepareResponseObject(objResponse, responseCode);
        return getAPIResponse(objRequest, objResponse);
    }

    protected ResponseEntity getAPIResponse(CommonRequest objRequest, CommonResponse objResponse)
    {
        objResponse.setServiceId(serviceID);
        objResponse.setTimeStamp(timeStamp);
        objResponse.setChannelTransactionId(objRequest.getChannelName() + "-" + objRequest.getChannelTransactionId());
        return getHttpResponse(objResponse);
    }

    protected void prepareResponseObject(CommonResponse objResponse, CommonEnum.ResponseCode responseCode)
    {
        objResponse.setResponseCode(responseCode.getCode());
        objResponse.setResponseMessage(responseCode.getMessage());
    }

    protected ResponseEntity getHttpResponse(CommonResponse objResponse)
    {
        //prepareResponseObject(objResponse, responseCode);
        CommonEnum.ResponseCode responseCode = null;

        for(CommonEnum.ResponseCode rsCode : CommonEnum.ResponseCode.values())
        {
            if(rsCode.getCode().equals(objResponse.getResponseCode())) {
                responseCode = rsCode;
                break;
            }
        }

        if (responseCode == CommonEnum.ResponseCode.REQUEST_SUCCESS)
            return ResponseEntity.ok(objResponse);
        else if (responseCode == CommonEnum.ResponseCode.REQUEST_ERROR)
            return ResponseEntity.badRequest().body(objResponse);
        else if (responseCode == CommonEnum.ResponseCode.AUTH_ERROR)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(objResponse);
        else if (responseCode == CommonEnum.ResponseCode.NOT_FOUND)
            return ResponseEntity.ok(objResponse);
            //return ResponseEntity.status(HttpStatus.NOT_FOUND).body(objResponse);
        else if (responseCode == CommonEnum.ResponseCode.DB_ERROR)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objResponse);
        else if (responseCode == CommonEnum.ResponseCode.AXIS_FAULT)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objResponse);
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objResponse);

    }


    protected ResponseEntity checkResponseSimpleData(CommonRequest objRequest, CommonResponse objResponse, int result, String method) {
        // result == -100 means the objResponse itself contains the response code and message
        if (result == -100) {
            return logAndGetAPIResponse(objRequest, objResponse);
        } else if (result > 0) {
            return logAndGetAPIResponse(objRequest, objResponse, CommonEnum.ResponseCode.REQUEST_SUCCESS);
        } else {
            if (method.equals("GET"))
                return logAndGetAPIResponse(objRequest, objResponse, CommonEnum.ResponseCode.NOT_FOUND);
            else
                return logAndGetAPIResponse(objRequest, objResponse, CommonEnum.ResponseCode.REQUEST_NOT_SUCCESS);
        }
    }

    protected ResponseEntity handleException(CommonRequest objRequest, CommonResponse objResponse, Exception ex) {
        if (ex instanceof MiddlewareExpApiException)
            return handleException(objRequest, objResponse, ex, ((MiddlewareExpApiException) ex).getErrorCode());
        else if (ex instanceof SQLException)
            return handleException(objRequest, objResponse, ex, CommonEnum.ResponseCode.DB_ERROR);
        else if (ex instanceof RemoteException)
            return handleException(objRequest, objResponse, ex, CommonEnum.ResponseCode.AXIS_FAULT);
        else if(ex instanceof ExecutionControl.NotImplementedException)
            return handleException(objRequest, objResponse, ex, CommonEnum.ResponseCode.NOT_FOUND);
        else
            return handleException(objRequest, objResponse, ex, CommonEnum.ResponseCode.REQUEST_ERROR);
    }

    protected ResponseEntity handleException(CommonRequest objRequest, CommonResponse objResponse, Exception ex, CommonEnum.ResponseCode responseCode) {
        prepareResponseObject(objResponse, responseCode);
        objResponse.setResponseMessage(ex.toString());
        //loggerUtility.logError(serviceID, ex);
        return logAndGetAPIResponse(objRequest, objResponse);
    }
}
