package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.*;
import com.example.OnlineHelpDesk.repository.ConversationRepository;
import com.example.OnlineHelpDesk.repository.FacilityRepository;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
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
            return ResponseEntity.ok().body(new MessageResponseVo(false, "Request has been created."));
        }
        return ResponseEntity.badRequest().body(new MessageResponseVo(true, "Facility does not exist."));
    }

    @GetMapping("/request")
    public ResponseEntity<List<RequestVo>> getRequests(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<RequestVo> requestVos = new ArrayList<>();
        if (user.getRole() == Role.ASSIGNEE){
            List<Request> requests = requestRepository.findAllByAssignedUserId(user.getId());
            for (Request request : requests) {
                requestVos.add(formatRequest(request));
            }
            return ResponseEntity.ok(requestVos);
        }
        else if (user.getRole() == Role.ADMIN){
            List<Request> requests = requestRepository.findAll();
            for (Request request : requests) {
                requestVos.add(formatRequest(request));
            }
            return ResponseEntity.ok(requestVos);
        }
        else if (user.getRole() == Role.FACILITY_HEAD){
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
                                          @RequestBody ConversationRequestVo conversationRequestVo){

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

        return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
    }

    @PutMapping("/reject-request/{id}")
    public ResponseEntity<?> rejectRequest(@PathVariable Integer id, Authentication authentication,
                                          @RequestBody ConversationRequestVo conversationRequestVo){

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

        return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
    }

    @PutMapping("/mark-in-progress/{id}")
    public ResponseEntity<?> markInProgressRequest(@PathVariable Integer id, Authentication authentication,
                                          @RequestBody ConversationRequestVo conversationRequestVo){

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

        return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
    }

    @PutMapping("/assign-request/{id}")
    public ResponseEntity<?> assignRequest(@PathVariable Integer id, Authentication authentication,
                                                   @RequestBody AssignRequestVo assignRequestVo){

        Request request = requestRepository.findById(id).get();
        if(assignRequestVo.getAssignedUserId() == null){
            request.setStatus(Status.UNASSIGNED);
            request.setAssignedUserId(null);
        }
        else{
            request.setStatus(Status.ASSIGNED);
            request.setAssignedUserId(assignRequestVo.getAssignedUserId());
        }
        requestRepository.save(request);

        return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been updated."));
    }

    @DeleteMapping("request/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer id, Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<Conversation> conversations = conversationRepository.findAllByRequestId(id);
        conversationRepository.deleteAll(conversations);
        Request request = requestRepository.findById(id).get();
        requestRepository.delete(request);
        return ResponseEntity.ok().body(new MessageResponseVo(true, "Request has been deleted."));
    }

    @PostMapping("/request/report")
    public ResponseEntity<?> requestReport(@RequestBody RequestReportVo requestReportVo){
        List<Request> requests = requestRepository.findRequestsByDateRangeAndFacility(requestReportVo.getStartDate(),
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
            if(request.getAssignedUserId()!=null){
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
