package com.teamfour.smartexpense.controller;

import com.teamfour.smartexpense.dto.AuthRequestDto;
import com.teamfour.smartexpense.dto.AuthResponseDto;
import com.teamfour.smartexpense.dto.RegisterRequestDto;
import com.teamfour.smartexpense.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * This method handles the login request.
     * It takes the user's login details, checks if they are correct,
     * and returns a response with login success info (like a token).
     *
     * @param authRequest the login details sent by the user (like email and password)
     * @return a response with login success message and other info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    /**
     * This method handles the registration request.
     * It takes the user's registration details, creates a new user account,
     * and returns a response with registration success info (like a token).
     *
     * @param registerRequest the details sent by the user to create a new account
     * @return a response with registration success message and other info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }
}
