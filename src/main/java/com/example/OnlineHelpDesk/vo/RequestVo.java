package com.example.OnlineHelpDesk.vo;

import com.example.OnlineHelpDesk.model.Facility;
import com.example.OnlineHelpDesk.model.Severity;
import com.example.OnlineHelpDesk.model.Status;
import com.example.OnlineHelpDesk.model.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestVo {

    private Integer id;

    private String title;

    private String description;

    private Severity severity;

    private Status status;

    private Date createdAt;

    private User createdUser;

    private User assignedUser;

    private Facility facility;
}
