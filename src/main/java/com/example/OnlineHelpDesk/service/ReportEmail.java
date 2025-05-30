package com.example.OnlineHelpDesk.service;

import com.example.OnlineHelpDesk.model.Request;
import com.example.OnlineHelpDesk.model.Role;
import com.example.OnlineHelpDesk.model.Status;
import com.example.OnlineHelpDesk.model.User;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportEmail {

    @Autowired
    EmailService emailService;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    UserRepository userRepository;

    @Scheduled(fixedRate = 180000)
    public void sendPeriodicEmails() {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);

        StringBuilder bodyBuilder = new StringBuilder("Daily Report for Requests:\n\n");

        for (Status status : Status.values()) {
            long count = requestRepository.countByStatus(status);
            bodyBuilder.append(String.format("%s: %d\n", formatStatusName(status), count));
        }

        String subject = "Daily Report for Requests";
        String body = bodyBuilder.toString();

        for (User user : admins) {
            emailService.sendEmail(user.getUsername(), subject, body);
        }
    }

    private String formatStatusName(Status status) {
        switch (status) {
            case UNASSIGNED:
                return "Unassigned";
            case ASSIGNED:
                return "Assigned";
            case WORK_IN_PROGRESS:
                return "Work In Progress";
            case COMPLETED:
                return "Completed";
            case REJECTED:
                return "Rejected";
        }
        return "";
    }
}
