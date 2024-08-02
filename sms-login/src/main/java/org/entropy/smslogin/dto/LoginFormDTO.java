package org.entropy.smslogin.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String phone;
    private String code;
}
