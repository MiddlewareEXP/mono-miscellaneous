package com.mono.miscellaneous.common.utilities;

public class CommonUtilities {
    public static String maskNumber(String num, int startlen, int endlen, char maskingChar) {

        int total = num.length();
        int masklen = total - (startlen + endlen);

        StringBuffer maskedbuf = new StringBuffer(num.substring(0, startlen));
        for (int i = 0; i < masklen; i++) {
            maskedbuf.append(maskingChar);
        }
        maskedbuf.append(num.substring(startlen + masklen, total));
        String masked = maskedbuf.toString();

        return masked;
    }
}
