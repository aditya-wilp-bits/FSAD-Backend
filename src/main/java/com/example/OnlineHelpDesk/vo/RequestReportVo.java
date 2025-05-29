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
public class RequestReportVo {

    private Integer facilityId;

    private Date startDate;

    private Date endDate;
}
