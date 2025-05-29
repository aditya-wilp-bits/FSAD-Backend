package com.example.OnlineHelpDesk.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConversationVo {

    private Integer id;

    private Integer requestId;

    private UserInfoResponseVo user;

    private String text;

    private Date createdAt;
}
