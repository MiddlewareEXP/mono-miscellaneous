package com.mono.miscellaneous.common.utilities;

public class CommonEnum {
    public enum ResponseCode
    {
        REQUEST_SUCCESS(100),
        NOT_FOUND(101),
        AUTH_ERROR(102),
        REQUEST_ERROR(103),
        AXIS_FAULT(104),
        CONN_ERROR(105),
        SECURITY_ERROR(106),
        DECLINE_FAULT(107),
        LOG_ERROR(108),
        ISO_ERROR(109),
        DB_ERROR(110),
        REVERSE_FLAG(111),
        REQUEST_NOT_SUCCESS(000);

        private int code;

        ResponseCode(int code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return String.valueOf(code);
        }

        public String getMessage()
        {
            String serviceMessage = "";

            switch (code) {
                case 100:  serviceMessage = "Operation Successful.";                			break;
                case 101:  serviceMessage = "No Record Found.";                     				break;
                case 102:  serviceMessage = "Authentication Failed.";                 			break;
                case 103:  serviceMessage = "One or Multiple Request Missing or not Valid.";      break;
                case 104:  serviceMessage = "Framework Error.";                   				break;
                case 105:  serviceMessage = "Error to Establish Connection";                      break;
                case 000:  serviceMessage = "Operation Not Successful.";                      	break;
                default:   serviceMessage 	= "Unknown Error."; 		                			break;
            }

            return serviceMessage;
        }

    }
}
