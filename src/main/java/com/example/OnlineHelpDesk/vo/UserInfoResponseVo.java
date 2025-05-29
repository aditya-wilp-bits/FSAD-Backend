package com.example.OnlineHelpDesk.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseVo {

    private Integer id;

    private String email;

    private String name;

    private String facility;

    private Integer facilityId;

    private String firstName;

    private String lastName;

    private Integer activeRequests;
}
