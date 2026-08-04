package com.jiubuntu.redislab.authcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendCodeResponse {
    private String message;
    private String code;
    private Long expiresIn;
}
