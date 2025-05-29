package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.Role;
import com.example.OnlineHelpDesk.model.User;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.service.AuthenticationService;
import com.example.OnlineHelpDesk.vo.ChangePasswordVo;
import com.example.OnlineHelpDesk.vo.LoginRequestVo;
import com.example.OnlineHelpDesk.vo.MessageResponseVo;
import com.example.OnlineHelpDesk.vo.RegisterUserRequestVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequestVo userRequest,
                                                           BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPassword(userRequest.getPassword());
        user.setUsername(userRequest.getEmail());
        user.setRole(Role.USER);
        authService.register(user);
        return ResponseEntity.ok(authService.authenticate(new LoginRequestVo(user.getUsername(), user.getPassword())));
    }

    @PostMapping("/admin/register-facility-head")
    public ResponseEntity<?> registerFacilityHead(@Valid @RequestBody RegisterUserRequestVo userRequest,
                                      BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPassword(userRequest.getPassword());
        user.setUsername(userRequest.getEmail());
        user.setFacilityId(userRequest.getFacilityId());
        user.setRole(Role.FACILITY_HEAD);
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/facility-head/register-facility-worker")
    public ResponseEntity<?> registerFacilityWorker(@Valid @RequestBody RegisterUserRequestVo userRequest,
                                                  BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        User facility_head = (User) authentication.getPrincipal();
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPassword(userRequest.getPassword());
        user.setUsername(userRequest.getEmail());
        user.setFacilityId(facility_head.getFacilityId());
        user.setRole(Role.ASSIGNEE);
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestVo loginRequestVo,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return ResponseEntity.ok(authService.authenticate(loginRequestVo));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(authService.logout(user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordVo changePasswordVo, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!passwordEncoder.matches(changePasswordVo.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponseVo(true, "Old password is incorrect"));
        }
        user.setPassword(passwordEncoder.encode(changePasswordVo.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseVo(false, "Password changed successfully!"));
    }
}
