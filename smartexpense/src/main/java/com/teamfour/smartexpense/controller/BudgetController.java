package com.teamfour.smartexpense.controller;

import com.teamfour.smartexpense.dto.BudgetRequestDto;
import com.teamfour.smartexpense.dto.BudgetResponseDto;
import com.teamfour.smartexpense.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> createBudget(@Valid @RequestBody BudgetRequestDto budgetRequest) {
        BudgetResponseDto budgetResponse = budgetService.createBudget(budgetRequest);
        return new ResponseEntity<>(budgetResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> getBudget(@PathVariable Long id) {
        BudgetResponseDto budgetResponse = budgetService.getBudget(id);
        return ResponseEntity.ok(budgetResponse);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getCurrentUserBudgets() {
        List<BudgetResponseDto> budgets = budgetService.getCurrentUserBudgets();
        return ResponseEntity.ok(budgets);
    }


    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getCurrentUserActiveBudgets() {
        List<BudgetResponseDto> budgets = budgetService.getCurrentUserActiveBudgets();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByCategory(@PathVariable Long categoryId) {
        List<BudgetResponseDto> budgets = budgetService.getBudgetsByCategory(categoryId);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BudgetResponseDto> budgets = budgetService.getBudgetsByDateRange(startDate, endDate);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDto budgetRequest) {
        BudgetResponseDto response = budgetService.updateBudget(id, budgetRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
