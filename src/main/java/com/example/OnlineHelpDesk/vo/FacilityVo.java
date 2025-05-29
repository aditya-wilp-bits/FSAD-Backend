package com.example.OnlineHelpDesk.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacilityVo {

    private Integer id;

    private String name;

    private String location;

    private String description;

    private Integer requestCount;
}
