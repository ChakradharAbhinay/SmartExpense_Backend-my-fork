package com.teamfour.smartexpense.service;

import com.teamfour.smartexpense.dto.AuthRequestDto;
import com.teamfour.smartexpense.dto.AuthResponseDto;
import com.teamfour.smartexpense.dto.RegisterRequestDto;
import com.teamfour.smartexpense.dto.UserDto;
import com.teamfour.smartexpense.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                       JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    public AuthResponseDto login(AuthRequestDto authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        final UserDto userDto = userService.getUserByUsername(authRequest.getUsername());

        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setUser(userDto);
        return response;
    }

    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        UserDto userDto = userService.registerUser(registerRequest);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setUser(userDto);
        return response;
    }
}