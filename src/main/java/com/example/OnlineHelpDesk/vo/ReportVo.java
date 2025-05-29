package com.example.OnlineHelpDesk.vo;

import com.example.OnlineHelpDesk.model.Severity;
import com.example.OnlineHelpDesk.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportVo {

    private Integer id;

    private String title;

    private Severity severity;

    private Status status;

    private Date createdAt;

    private String facility;

    private String createdBy;

    private String assignedTo;

    private String resolutionTime;
}
