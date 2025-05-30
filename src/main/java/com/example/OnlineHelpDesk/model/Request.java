package com.example.OnlineHelpDesk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private Integer facilityId;

    private String title;

    private String description;

    @Column(columnDefinition = "datetime")
    private Date createdAt;

    @Enumerated(value = EnumType.STRING)
    private Severity severity;

    @Enumerated(value = EnumType.STRING)
    private Status status;

    private Integer assignedUserId;
}
