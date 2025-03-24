package com.teamfour.smartexpense.service;

import com.teamfour.smartexpense.model.Budget;
import com.teamfour.smartexpense.repository.BudgetRespository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRespository budgetRespository;

    public BudgetService(BudgetRespository budgetRespository) {
        this.budgetRespository = budgetRespository;
    }

    public void checkBudgetLimits(Long userId, Long categoryId, BigDecimal expenseAmount) {
        // Get active budgets for this user and category
        LocalDate today = LocalDate.now();
        List<Budget> applicableBudgets = budgetRespository
                .findByUserIdAndCategoryId(userId, categoryId);

        // Filter budgets that are active for today
        applicableBudgets = applicableBudgets.stream()
                .filter(budget -> !today.isBefore(budget.getStartDate()) && !today.isAfter(budget.getEndDate()))
                .toList();

        // Update spent amount and check for thresholds
        for (Budget budget : applicableBudgets) {
            BigDecimal currentSpent = budget.getSpentAmount() != null ? budget.getSpentAmount() : BigDecimal.ZERO;
            BigDecimal newSpentAmount = currentSpent.add(expenseAmount);
            budget.setSpentAmount(newSpentAmount);

            // Check if budget threshold is exceeded
            // You can add notification logic here
            if (newSpentAmount.compareTo(budget.getAmount()) > 0) {
                // Budget exceeded - could trigger notification
                System.out.println("Budget exceeded for category: " + categoryId);
                // notificationService.sendBudgetAlert(budget);
            } else if (newSpentAmount.multiply(new BigDecimal("0.8")).compareTo(budget.getAmount()) > 0) {
                // Over 80% of budget used - could trigger a warning
                System.out.println("80% of budget used for category: " + categoryId);
                // notificationService.sendBudgetWarning(budget);
            }

            budgetRespository.save(budget);
        }
    }
}
