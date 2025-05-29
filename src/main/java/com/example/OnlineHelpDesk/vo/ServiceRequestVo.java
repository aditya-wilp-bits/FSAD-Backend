package com.example.OnlineHelpDesk.vo;

import com.example.OnlineHelpDesk.model.Facility;
import com.example.OnlineHelpDesk.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestVo {

    @NotNull(message = "Facility ID is required")
    private Integer facilityId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;
}
