package com.teamfour.smartexpense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfour.smartexpense.dto.AuthRequestDto;
import com.teamfour.smartexpense.dto.AuthResponseDto;
import com.teamfour.smartexpense.dto.RegisterRequestDto;
import com.teamfour.smartexpense.security.JwtRequestFilter;
import com.teamfour.smartexpense.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthResponseDto dummyResponse;

    @BeforeEach
    void setUp() {
        dummyResponse = new AuthResponseDto();
        dummyResponse.setToken("mock-token");
    }

    @Test
    void testLogin() throws Exception {
        AuthRequestDto authRequest = new AuthRequestDto();
        authRequest.setUsername("test@example.com");
        authRequest.setPassword("password");

        Mockito.when(authService.login(authRequest)).thenReturn(dummyResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("new@example.com");
        registerRequest.setUsername("dummyuser");
        registerRequest.setPassword("newpass");

        Mockito.when(authService.register(registerRequest)).thenReturn(dummyResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }
}
