package com.example.OnlineHelpDesk.vo;

import com.example.OnlineHelpDesk.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseVo {

    private String token;

    private String message;

    private User user;
}
