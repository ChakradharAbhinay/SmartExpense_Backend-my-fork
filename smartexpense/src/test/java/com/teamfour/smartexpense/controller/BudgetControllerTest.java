package com.teamfour.smartexpense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfour.smartexpense.dto.BudgetRequestDto;
import com.teamfour.smartexpense.dto.BudgetResponseDto;
import com.teamfour.smartexpense.security.JwtUtil;
import com.teamfour.smartexpense.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BudgetController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testCreateBudget() throws Exception {
        BudgetRequestDto requestDto = new BudgetRequestDto();
        requestDto.setAmount(BigDecimal.valueOf(1000.0));
        requestDto.setCategoryId(1L);
        requestDto.setStartDate(LocalDate.now());
        requestDto.setEndDate(LocalDate.now().plusDays(30));

        BudgetResponseDto responseDto = new BudgetResponseDto();
        responseDto.setId(1L);
        responseDto.setAmount(BigDecimal.valueOf(1000.0));
        responseDto.setCategoryId(1L);

        when(budgetService.createBudget(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetBudgetById() throws Exception {
        long budgetId = 1L;
        BudgetResponseDto responseDto = new BudgetResponseDto();
        responseDto.setId(budgetId);
        responseDto.setAmount(BigDecimal.valueOf(500.0));

        when(budgetService.getBudget(budgetId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/budgets/{id}", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId));
    }

    @Test
    void testGetAllBudgetsForUser() throws Exception {
        BudgetResponseDto dto1 = new BudgetResponseDto();
        dto1.setId(1L);
        BudgetResponseDto dto2 = new BudgetResponseDto();
        dto2.setId(2L);

        when(budgetService.getCurrentUserBudgets()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
