package com.example.OnlineHelpDesk.service;

import com.example.OnlineHelpDesk.model.Token;
import com.example.OnlineHelpDesk.model.User;
import com.example.OnlineHelpDesk.repository.TokenRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.vo.LoginRequestVo;
import com.example.OnlineHelpDesk.vo.LoginResponseVo;
import com.example.OnlineHelpDesk.vo.MessageResponseVo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 TokenRepository tokenRepository,
                                 AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }

    public MessageResponseVo register(User request) {

        if(repository.findByUsername(request.getUsername()).isPresent()) {
            return new MessageResponseVo(true, "Email Id already exist");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFacilityId(request.getFacilityId());
        user = repository.save(user);

        return new MessageResponseVo(false, "User registration was successful");
    }

    public LoginResponseVo authenticate(LoginRequestVo request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getEmail()).orElseThrow();
        String jwt = jwtService.generateToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(jwt, user);

        return new LoginResponseVo(jwt, "User login was successful", user);

    }

    public MessageResponseVo logout(User user) {
        revokeAllTokenByUser(user);
        return new MessageResponseVo(true, "User logged out successfully");
    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String jwt, User user) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }
}
