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

    /**
     * This method allows the user to create a new budget.
     * The request body contains the budget details.
     *
     * @param budgetRequest the details of the budget to be created
     * @return the created budget and HTTP status 201
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> createBudget(@Valid @RequestBody BudgetRequestDto budgetRequest) {
        BudgetResponseDto budgetResponse = budgetService.createBudget(budgetRequest);
        return new ResponseEntity<>(budgetResponse, HttpStatus.CREATED);
    }

    /**
     * This method returns the budget with the given ID.
     *
     * @param id the ID of the budget
     * @return the matching budget details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> getBudget(@PathVariable Long id) {
        BudgetResponseDto budgetResponse = budgetService.getBudget(id);
        return ResponseEntity.ok(budgetResponse);
    }

    /**
     * This method returns all budgets created by the current logged-in user.
     *
     * @return list of budgets belonging to the user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getCurrentUserBudgets() {
        List<BudgetResponseDto> budgets = budgetService.getCurrentUserBudgets();
        return ResponseEntity.ok(budgets);
    }

    /**
     * This method returns all *active* budgets of the current user.
     * Active budgets could mean budgets that are currently valid or ongoing.
     *
     * @return list of active budgets
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getCurrentUserActiveBudgets() {
        List<BudgetResponseDto> budgets = budgetService.getCurrentUserActiveBudgets();
        return ResponseEntity.ok(budgets);
    }

    /**
     * This method returns budgets filtered by a specific category ID.
     *
     * @param categoryId the category to filter by
     * @return list of budgets under that category
     */
    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByCategory(@PathVariable Long categoryId) {
        List<BudgetResponseDto> budgets = budgetService.getBudgetsByCategory(categoryId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * This method returns budgets that fall between the given start and end dates.
     *
     * @param startDate the beginning of the date range
     * @param endDate the end of the date range
     * @return list of budgets within the date range
     */
    @GetMapping("/by-date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BudgetResponseDto> budgets = budgetService.getBudgetsByDateRange(startDate, endDate);
        return ResponseEntity.ok(budgets);
    }

    /**
     * This method updates the details of an existing budget with the given ID.
     *
     * @param id the ID of the budget to update
     * @param budgetRequest the updated budget details
     * @return the updated budget
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDto budgetRequest) {
        BudgetResponseDto response = budgetService.updateBudget(id, budgetRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * This method deletes the budget with the given ID.
     *
     * @param id the ID of the budget to delete
     * @return a response with no content if deletion is successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
