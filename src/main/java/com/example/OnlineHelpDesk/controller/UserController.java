package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.*;
import com.example.OnlineHelpDesk.repository.FacilityRepository;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.service.EmailService;
import com.example.OnlineHelpDesk.vo.DashboardDataVo;
import com.example.OnlineHelpDesk.vo.MessageResponseVo;
import com.example.OnlineHelpDesk.vo.RegisterUserRequestVo;
import com.example.OnlineHelpDesk.vo.UserInfoResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private RequestRepository requestRepository;

    @GetMapping("/admin/facility-head")
    public ResponseEntity<?> getFacilityHead() {
        List<UserInfoResponseVo> facility_head_response = new ArrayList<>();
        List<User> facility_head = userRepository.findAllByRole(Role.FACILITY_HEAD);
        for (User user : facility_head) {
            UserInfoResponseVo userInfoResponseVo = new UserInfoResponseVo();
            userInfoResponseVo.setId(user.getId());
            userInfoResponseVo.setEmail(user.getUsername());
            userInfoResponseVo.setName(user.getFirstName() + " " + user.getLastName());
            userInfoResponseVo.setFirstName(user.getFirstName());
            userInfoResponseVo.setLastName(user.getLastName());
            userInfoResponseVo.setFacilityId(user.getFacilityId());
            Facility facility = facilityRepository.findById(user.getFacilityId()).get();
            userInfoResponseVo.setFacility(facility.getName());
            facility_head_response.add(userInfoResponseVo);
        }
        return ResponseEntity.ok(facility_head_response);
    }

    @GetMapping("/facility-head/assignee")
    public ResponseEntity<?> getAssignee(Authentication authentication) {
        List<UserInfoResponseVo> assignee_response = new ArrayList<>();
        User facilityHead = (User) authentication.getPrincipal();
        List<User> facility_worker = userRepository.findAllByRoleAndFacilityId(Role.ASSIGNEE, facilityHead.getFacilityId());
        for (User user : facility_worker) {
            UserInfoResponseVo userInfoResponseVo = new UserInfoResponseVo();
            userInfoResponseVo.setId(user.getId());
            userInfoResponseVo.setEmail(user.getUsername());
            userInfoResponseVo.setName(user.getFirstName() + " " + user.getLastName());
            userInfoResponseVo.setFirstName(user.getFirstName());
            userInfoResponseVo.setLastName(user.getLastName());
            userInfoResponseVo.setActiveRequests((int) requestRepository.findAllByAssignedUserId(user.getId()).stream().count());
            assignee_response.add(userInfoResponseVo);
        }
        return ResponseEntity.ok(assignee_response);
    }

    @PutMapping("/admin/facility-head/{id}")
    public ResponseEntity<?> updateFacilityHead(@PathVariable Integer id, @RequestBody RegisterUserRequestVo userRequestVo){
        User user = userRepository.findById(id).get();
        user.setFirstName(userRequestVo.getFirstName());
        user.setLastName(userRequestVo.getLastName());
        user.setFacilityId(userRequestVo.getFacilityId());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseVo(false, "User updated successfully"));
    }

    @PutMapping("/facility-head/assignee/{id}")
    public ResponseEntity<?> updateAssignee(@PathVariable Integer id, @RequestBody RegisterUserRequestVo userRequestVo){
        User user = userRepository.findById(id).get();
        user.setFirstName(userRequestVo.getFirstName());
        user.setLastName(userRequestVo.getLastName());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseVo(false, "Assignee updated successfully"));
    }

    @DeleteMapping("/admin/facility-head/{id}")
    public ResponseEntity<?> deleteFacilityHead(@PathVariable Integer id){
        User user = userRepository.findById(id).get();
        userRepository.delete(user);
        return ResponseEntity.ok(new MessageResponseVo(true, "Facility head deleted successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        DashboardDataVo dashboardDataVo = new DashboardDataVo();
        if (user.getRole() == Role.ADMIN) {
            dashboardDataVo.setTotalRequests(requestRepository.count());
            dashboardDataVo.setCloseRequests(requestRepository.countByStatus(Status.COMPLETED));
            dashboardDataVo.setOpenRequests(requestRepository.count()-requestRepository.countByStatus(Status.COMPLETED));
            dashboardDataVo.setUnAssignedRequests(requestRepository.countByStatus(Status.UNASSIGNED));
            dashboardDataVo.setInProgressRequests(requestRepository.countByStatus(Status.WORK_IN_PROGRESS));
        }
        if (user.getRole() == Role.FACILITY_HEAD) {
            dashboardDataVo.setTotalRequests(requestRepository.countAllByFacilityId(user.getFacilityId()));
            dashboardDataVo.setCloseRequests(requestRepository.countAllByFacilityIdAndStatus(user.getFacilityId(), Status.COMPLETED));
            dashboardDataVo.setOpenRequests(requestRepository.countAllByFacilityId(user.getFacilityId())-requestRepository.countAllByFacilityIdAndStatus(user.getFacilityId(), Status.COMPLETED));
            dashboardDataVo.setUnAssignedRequests(requestRepository.countAllByFacilityIdAndStatus(user.getFacilityId(), Status.UNASSIGNED));
            dashboardDataVo.setInProgressRequests(requestRepository.countAllByFacilityIdAndStatus(user.getFacilityId(), Status.WORK_IN_PROGRESS));
        }
        if (user.getRole() == Role.ASSIGNEE) {
            dashboardDataVo.setTotalRequests(requestRepository.countAllByAssignedUserId(user.getFacilityId()));
            dashboardDataVo.setCloseRequests(requestRepository.countByAssignedUserIdAndStatus(user.getFacilityId(), Status.COMPLETED));
            dashboardDataVo.setOpenRequests(requestRepository.countAllByAssignedUserId(user.getFacilityId())-requestRepository.countByAssignedUserIdAndStatus(user.getFacilityId(), Status.COMPLETED));
            dashboardDataVo.setUnAssignedRequests(requestRepository.countByAssignedUserIdAndStatus(user.getFacilityId(), Status.UNASSIGNED));
            dashboardDataVo.setInProgressRequests(requestRepository.countByAssignedUserIdAndStatus(user.getFacilityId(), Status.WORK_IN_PROGRESS));
        }
        if (user.getRole() == Role.USER) {
            dashboardDataVo.setTotalRequests(requestRepository.countAllByUserId(user.getId()));
            dashboardDataVo.setCloseRequests(requestRepository.countAllByUserIdAndStatus(user.getId(), Status.COMPLETED));
            dashboardDataVo.setOpenRequests(requestRepository.countAllByUserId(user.getId())-requestRepository.countByAssignedUserIdAndStatus(user.getFacilityId(), Status.COMPLETED));
            dashboardDataVo.setUnAssignedRequests(requestRepository.countAllByUserIdAndStatus(user.getId(), Status.UNASSIGNED));
            dashboardDataVo.setInProgressRequests(requestRepository.countAllByUserIdAndStatus(user.getId(), Status.WORK_IN_PROGRESS));
        }
        return ResponseEntity.ok(dashboardDataVo);
    }
}
