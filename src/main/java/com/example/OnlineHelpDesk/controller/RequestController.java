package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.*;
import com.example.OnlineHelpDesk.repository.ConversationRepository;
import com.example.OnlineHelpDesk.repository.FacilityRepository;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.service.EmailService;
import com.example.OnlineHelpDesk.vo.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
public class RequestController {

        @Autowired
        RequestRepository requestRepository;

        @Autowired
        FacilityRepository facilityRepository;

        @Autowired
        UserRepository userRepository;

        @Autowired
        EmailService emailService;

        @Autowired
        ConversationRepository conversationRepository;

        @PostMapping("/request")
        public ResponseEntity<?> createRequest(@Valid @RequestBody ServiceRequestVo serviceRequest,
                        BindingResult result, Authentication authentication) {
                if (result.hasErrors()) {
                        return ResponseEntity.badRequest().body(result.getAllErrors());
                }
                User user = (User) authentication.getPrincipal();
                if (facilityRepository.existsById(serviceRequest.getFacilityId())) {
                        Request request = new Request();
                        request.setFacilityId(serviceRequest.getFacilityId());
                        request.setUserId(user.getId());
                        request.setTitle(serviceRequest.getTitle());
                        request.setDescription(serviceRequest.getDescription());
                        request.setSeverity(serviceRequest.getSeverity());
                        request.setStatus(Status.UNASSIGNED);
                        request.setCreatedAt(new Date());
                        requestRepository.save(request);

                        Facility facility = facilityRepository.findById(serviceRequest.getFacilityId()).get();

                        String userEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "Your request has been created successfully with the following details:\n\n"
                                                        +
                                                        "- Facility : %s\n" +
                                                        "- Title: %s\n" +
                                                        "- Description: %s\n" +
                                                        "- Severity: %s\n" +
                                                        "- Status: %s\n" +
                                                        "- Created At: %s\n\n" +
                                                        "Our team will review your request and take the necessary action shortly.\n\n"
                                                        +
                                                        "Thank you for reaching out.\n\n" +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        user.getFirstName() + " " + user.getLastName(),
                                        facility.getName(),
                                        request.getTitle(),
                                        request.getDescription(),
                                        request.getSeverity(),
                                        request.getStatus(),
                                        request.getCreatedAt().toString());

                        String facilityHeadEmailBody = String.format(
                                        "Dear Facility Head,\n\n" +
                                                        "A new service request has been submitted by %s with the following details:\n\n"
                                                        +
                                                        "- Title: %s\n" +
                                                        "- Description: %s\n" +
                                                        "- Severity: %s\n" +
                                                        "- Status: %s\n" +
                                                        "- Created At: %s\n\n" +
                                                        "Please review and take the appropriate action.\n\n" +
                                                        "Best regards,\n" +
                                                        "Service Request System",
                                        user.getFirstName() + " " + user.getLastName(),
                                        request.getTitle(),
                                        request.getDescription(),
                                        request.getSeverity(),
                                        request.getStatus(),
                                        request.getCreatedAt().toString());

                        emailService.sendEmail(user.getUsername(), "Request Created Successfully", userEmailBody);

                        List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                        request.getFacilityId());
                        for (User facilityHeadUser : facilityHead) {
                                emailService.sendEmail(facilityHeadUser.getUsername(), "New Request have been Created",
                                                facilityHeadEmailBody);
                        }

                        return ResponseEntity.ok().body(new MessageResponseVo(false, "Request has been created."));
                }
                return ResponseEntity.badRequest().body(new MessageResponseVo(true, "Facility does not exist."));
        }

        @GetMapping("/request")
        public ResponseEntity<List<RequestVo>> getRequests(Authentication authentication) {
                User user = (User) authentication.getPrincipal();
                List<RequestVo> requestVos = new ArrayList<>();
                if (user.getRole() == Role.ASSIGNEE) {
                        List<Request> requests = requestRepository.findAllByAssignedUserId(user.getId());
                        for (Request request : requests) {
                                requestVos.add(formatRequest(request));
                        }
                        return ResponseEntity.ok(requestVos);
                } else if (user.getRole() == Role.ADMIN) {
                        List<Request> requests = requestRepository.findAll();
                        for (Request request : requests) {
                                requestVos.add(formatRequest(request));
                        }
                        return ResponseEntity.ok(requestVos);
                } else if (user.getRole() == Role.FACILITY_HEAD) {
                        List<Request> requests = requestRepository.findAllByFacilityId(user.getFacilityId());
                        for (Request request : requests) {
                                requestVos.add(formatRequest(request));
                        }
                        return ResponseEntity.ok(requestVos);
                }
                List<Request> requests = requestRepository.findAllByUserId(user.getId());
                for (Request request : requests) {
                        requestVos.add(formatRequest(request));
                }
                return ResponseEntity.ok(requestVos);
        }

        @GetMapping("/request/{id}")
        public ResponseEntity<?> getRequestById(@PathVariable Integer id) {
                Request request = requestRepository.findById(id).get();
                return ResponseEntity.ok(formatRequest(request));
        }

        @PutMapping("/close-request/{id}")
        public ResponseEntity<?> closeRequest(@PathVariable Integer id, Authentication authentication,
                        @RequestBody ConversationRequestVo conversationRequestVo) {

                User user = (User) authentication.getPrincipal();
                Conversation conversation = new Conversation();
                conversation.setRequestId(id);
                conversation.setMessage("Close Request Reason :-" + conversationRequestVo.getText());
                conversation.setCreatedAt(new Date());
                conversation.setUserId(user.getId());
                conversationRepository.save(conversation);

                Request request = requestRepository.findById(id).get();
                request.setStatus(Status.COMPLETED);
                requestRepository.save(request);

                User createdUser = userRepository.findById(request.getUserId()).get();

                String assigneeEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "You have success closed the request with the Title - %s created by %s \n\n"
                                                +
                                                "Best regards,\n" +
                                                "Support Team",
                                user.getFirstName() + " " + user.getLastName(),
                                request.getTitle(),
                                createdUser.getFirstName() + " " + createdUser.getLastName());
                emailService.sendEmail(user.getUsername(), "Request Closed Successfully", assigneeEmailBody);

                String userEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "Your request with the Title - %s has been successfully closed by %s\n\n"
                                                +
                                                "Best regards,\n" +
                                                "Support Team",
                                createdUser.getFirstName() + " " + createdUser.getLastName(),
                                request.getTitle(),
                                user.getFirstName() + " " + user.getLastName());
                emailService.sendEmail(createdUser.getUsername(), "Request Closed Successfully", userEmailBody);

                List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                request.getFacilityId());
                for (User facilityHeadUser : facilityHead) {
                        String facilityHeadEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "The request with the Title - %s, created by %s, has been successfully closed by %s\n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        facilityHeadUser.getFirstName() + " " + facilityHeadUser.getLastName(),
                                        request.getTitle(),
                                        createdUser.getFirstName() + " " + createdUser.getLastName(),
                                        user.getFirstName() + " " + user.getLastName());
                        emailService.sendEmail(facilityHeadUser.getUsername(), "Request Closed", facilityHeadEmailBody);
                }

                return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
        }

        @PutMapping("/reject-request/{id}")
        public ResponseEntity<?> rejectRequest(@PathVariable Integer id, Authentication authentication,
                        @RequestBody ConversationRequestVo conversationRequestVo) {

                User user = (User) authentication.getPrincipal();
                Conversation conversation = new Conversation();
                conversation.setRequestId(id);
                conversation.setMessage("Request Rejection Reason  :- " + conversationRequestVo.getText());
                conversation.setCreatedAt(new Date());
                conversation.setUserId(user.getId());
                conversationRepository.save(conversation);

                Request request = requestRepository.findById(id).get();
                request.setStatus(Status.REJECTED);
                requestRepository.save(request);

                User createdUser = userRepository.findById(request.getUserId()).get();

                String userEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "Your request with the Title %s has been rejected by %s\n\n" +
                                                "Best regards,\n" +
                                                "Support Team",
                                createdUser.getFirstName() + " " + createdUser.getLastName(),
                                request.getTitle(),
                                user.getFirstName() + " " + user.getLastName());
                emailService.sendEmail(createdUser.getUsername(), "Request Rejected", userEmailBody);

                List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                request.getFacilityId());
                for (User facilityHeadUser : facilityHead) {
                        String facilityHeadEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "The request with the Title - %s, created by %s, has been rejected closed by %s\n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        facilityHeadUser.getFirstName() + " " + facilityHeadUser.getLastName(),
                                        request.getTitle(),
                                        createdUser.getFirstName() + " " + createdUser.getLastName(),
                                        Objects.equals(facilityHeadUser.getId(), user.getId()) ? "you"
                                                        : user.getFirstName() + " " + user.getLastName());
                        emailService.sendEmail(facilityHeadUser.getUsername(), "Request Rejected",
                                        facilityHeadEmailBody);
                }

                return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
        }

        @PutMapping("/mark-in-progress/{id}")
        public ResponseEntity<?> markInProgressRequest(@PathVariable Integer id, Authentication authentication,
                        @RequestBody ConversationRequestVo conversationRequestVo) {

                User user = (User) authentication.getPrincipal();
                Conversation conversation = new Conversation();
                conversation.setRequestId(id);
                conversation.setMessage("Description about Work :- " + conversationRequestVo.getText());
                conversation.setCreatedAt(new Date());
                conversation.setUserId(user.getId());
                conversationRepository.save(conversation);

                Request request = requestRepository.findById(id).get();
                request.setStatus(Status.WORK_IN_PROGRESS);
                requestRepository.save(request);

                User createdUser = userRepository.findById(request.getUserId()).get();

                String assigneeEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "You have success updated the status to In-Progress of the request with the Title - %s created by %s\n\n"
                                                +
                                                "Best regards,\n" +
                                                "Support Team",
                                user.getFirstName() + " " + user.getLastName(),
                                request.getTitle(),
                                createdUser.getFirstName() + " " + createdUser.getLastName());
                emailService.sendEmail(user.getUsername(), "Request Status Updated", assigneeEmailBody);

                String userEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "Your request with the Title - %s has moved to In-Progress by %s\n\n" +
                                                "Best regards,\n" +
                                                "Support Team",
                                createdUser.getFirstName() + " " + createdUser.getLastName(),
                                request.getTitle(),
                                user.getFirstName() + " " + user.getLastName());
                emailService.sendEmail(createdUser.getUsername(), "Request Status Update", userEmailBody);

                List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                request.getFacilityId());
                for (User facilityHeadUser : facilityHead) {
                        String facilityHeadEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "The request with the Title - %s, created by %s, has been moved to In-Progress by %s\n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        facilityHeadUser.getFirstName() + " " + facilityHeadUser.getLastName(),
                                        request.getTitle(),
                                        createdUser.getFirstName() + " " + createdUser.getLastName(),
                                        user.getFirstName() + " " + user.getLastName());
                        emailService.sendEmail(facilityHeadUser.getUsername(), "Request Status Updated",
                                        facilityHeadEmailBody);
                }

                return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
        }

        @PutMapping("/assign-request/{id}")
        public ResponseEntity<?> assignRequest(@PathVariable Integer id, Authentication authentication,
                        @RequestBody AssignRequestVo assignRequestVo) {

                Request request = requestRepository.findById(id).get();
                if (assignRequestVo.getAssignedUserId() == null) {
                        request.setStatus(Status.UNASSIGNED);
                        request.setAssignedUserId(null);
                } else {
                        request.setStatus(Status.ASSIGNED);
                        request.setAssignedUserId(assignRequestVo.getAssignedUserId());
                }
                requestRepository.save(request);

                User createdUser = userRepository.findById(request.getUserId()).get();
                User assignedUser = null;

                if (request.getStatus() == Status.ASSIGNED) {
                        assignedUser = userRepository.findById(assignRequestVo.getAssignedUserId()).get();
                        String assigneeEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "You have assigned a new request with the Title - %s created by %s \n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        assignedUser.getFirstName() + " " + assignedUser.getLastName(),
                                        request.getTitle(),
                                        createdUser.getFirstName() + " " + createdUser.getLastName());
                        emailService.sendEmail(assignedUser.getUsername(), "Assigned a new Request", assigneeEmailBody);
                }

                String userEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "Your request with the Title - %s has been %s \n\n" +
                                                "Best regards,\n" +
                                                "Support Team",
                                createdUser.getFirstName() + " " + createdUser.getLastName(),
                                request.getTitle(),
                                assignedUser != null
                                                ? "assigned to " + assignedUser.getFirstName() + " "
                                                                + assignedUser.getLastName()
                                                : "unassigned, A new worker will be assigned soon");
                emailService.sendEmail(createdUser.getUsername(),
                                assignedUser != null ? "Request Assigned" : "Request Unassigned", userEmailBody);

                List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                request.getFacilityId());
                for (User facilityHeadUser : facilityHead) {
                        String facilityHeadEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "The request with the Title - %s, created by %s, has been %s \n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        facilityHeadUser.getFirstName() + " " + facilityHeadUser.getLastName(),
                                        request.getTitle(),
                                        createdUser.getFirstName() + " " + createdUser.getLastName(),
                                        assignedUser != null
                                                        ? "assigned to " + assignedUser.getFirstName() + " "
                                                                        + assignedUser.getLastName()
                                                        : "unassigned, Please assign a new worker soon");
                        emailService.sendEmail(facilityHeadUser.getUsername(), "Request Assigned",
                                        facilityHeadEmailBody);
                }

                return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
        }

        @DeleteMapping("request/{id}")
        public ResponseEntity<?> deleteRequest(@PathVariable Integer id, Authentication authentication) {
                User user = (User) authentication.getPrincipal();
                List<Conversation> conversations = conversationRepository.findAllByRequestId(id);
                conversationRepository.deleteAll(conversations);
                Request request = requestRepository.findById(id).get();

                String userEmailBody = String.format(
                                "Dear %s,\n\n" +
                                                "Your request with the Title - %s has been deleted successfully\n\n" +
                                                "Best regards,\n" +
                                                "Support Team",
                                user.getFirstName() + " " + user.getLastName(),
                                request.getTitle());
                emailService.sendEmail(user.getUsername(), "Request Deleted Successfully", userEmailBody);

                List<User> facilityHead = userRepository.findAllByRoleAndFacilityId(Role.FACILITY_HEAD,
                                request.getFacilityId());
                for (User facilityHeadUser : facilityHead) {
                        String facilityHeadEmailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "The request with the Title - %s, created by %s, has been deleted\n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "Support Team",
                                        facilityHeadUser.getFirstName() + " " + facilityHeadUser.getLastName(),
                                        request.getTitle(),
                                        user.getFirstName() + " " + user.getLastName());
                        emailService.sendEmail(facilityHeadUser.getUsername(), "Request Deleted",
                                        facilityHeadEmailBody);
                }

                requestRepository.delete(request);
                return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been deleted."));
        }

        @PostMapping("/request/report")
        public ResponseEntity<?> requestReport(@RequestBody RequestReportVo requestReportVo) {
                List<Request> requests = requestRepository.findRequestsByDateRangeAndFacility(
                                requestReportVo.getStartDate(),
                                requestReportVo.getEndDate(), requestReportVo.getFacilityId());
                List<ReportVo> reportVoList = new ArrayList<>();
                for (Request request : requests) {
                        ReportVo reportVo = new ReportVo();
                        reportVo.setId(request.getId());
                        reportVo.setTitle(request.getTitle());
                        reportVo.setSeverity(request.getSeverity());
                        reportVo.setStatus(request.getStatus());
                        reportVo.setCreatedAt(request.getCreatedAt());
                        reportVo.setFacility(facilityRepository.findById(request.getFacilityId()).get().getName());
                        User createdBy = userRepository.findById(request.getUserId()).get();
                        reportVo.setCreatedBy(createdBy.getFirstName() + " " + createdBy.getLastName());
                        if (request.getAssignedUserId() != null) {
                                User assignedTo = userRepository.findById(request.getAssignedUserId()).get();
                                reportVo.setAssignedTo(assignedTo.getFirstName() + " " + assignedTo.getLastName());
                        }
                        reportVoList.add(reportVo);
                }
                return ResponseEntity.ok().body(reportVoList);
        }

        private RequestVo formatRequest(Request request) {
                RequestVo requestVo = new RequestVo();
                requestVo.setId(request.getId());
                requestVo.setTitle(request.getTitle());
                requestVo.setDescription(request.getDescription());
                requestVo.setStatus(request.getStatus());
                requestVo.setSeverity(request.getSeverity());
                requestVo.setCreatedAt(request.getCreatedAt());

                if (request.getAssignedUserId() != null) {
                        requestVo.setAssignedUser(userRepository.findById(request.getAssignedUserId()).get());
                }
                requestVo.setCreatedUser(userRepository.findById(request.getUserId()).get());
                requestVo.setFacility(facilityRepository.findById(request.getFacilityId()).get());
                return requestVo;
        }
}
