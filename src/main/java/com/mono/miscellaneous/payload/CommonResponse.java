package com.mono.miscellaneous.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse {
    private String responseCode;
    private String responseMsg;
}
