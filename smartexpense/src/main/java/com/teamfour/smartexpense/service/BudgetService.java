package com.teamfour.smartexpense.service;

import com.teamfour.smartexpense.dto.BudgetRequestDto;
import com.teamfour.smartexpense.dto.BudgetResponseDto;
import com.teamfour.smartexpense.exception.ResourceNotFoundException;
import com.teamfour.smartexpense.model.Budget;
import com.teamfour.smartexpense.model.Category;
import com.teamfour.smartexpense.model.Transaction;
import com.teamfour.smartexpense.model.User;
import com.teamfour.smartexpense.repository.BudgetRespository;
import com.teamfour.smartexpense.repository.CategoryRepository;
import com.teamfour.smartexpense.repository.TransactionRepository;
import com.teamfour.smartexpense.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRespository budgetRespository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRespository budgetRespository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRespository = budgetRespository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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

    /**
     * Creates a new budget
     */
    @Transactional
    public BudgetResponseDto createBudget(BudgetRequestDto requestDto) {
        // Get current user
        User currentUser = getCurrentUser();

        // Get Category
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));

        // Validate dates
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Create budget
        Budget budget = Budget.builder()
                .amount(requestDto.getAmount())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .spentAmount(BigDecimal.ZERO)
                .category(category)
                .user(currentUser)
                .build();

        // Save budget
        Budget savedBudget = budgetRespository.save(budget);

        // Calculate initial spent amount from existing transactions
        updateBudgetSpentAmount(savedBudget);

        return mapToDto(savedBudget);
    }

    /**
     * Gets a budget by ID
     */
    public BudgetResponseDto getBudget(Long id) {
        Budget budget = getBudgetEntity(id);
        return mapToDto(budget);
    }

    /**
     * Deletes a budget
     */
    @Transactional
    public void deleteBudget(Long id) {
        Budget budget = getBudgetEntity(id);

        // Validate user owns this budget
        User currentUser = getCurrentUser();
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to delete this budget");
        }

        budgetRespository.delete(budget);
    }

    /**
     * Updates the spent amount for a budget based on transactions
     */
    private void updateBudgetSpentAmount(Budget budget) {
        // Calculate total expense transactions for this category and date range
        List<Transaction> transactions = transactionRepository.findByCategoryId(budget.getCategory().getId());

        BigDecimal totalSpent = transactions.stream()
                .filter(t -> t.getType() == com.teamfour.smartexpense.model.TransactionType.EXPENSE)
                .filter(t -> !t.getDate().toLocalDate().isBefore(budget.getStartDate()))
                .filter(t -> !t.getDate().toLocalDate().isAfter(budget.getEndDate()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setSpentAmount(totalSpent);
        budgetRespository.save(budget);
    }

    /**
     * Gets a budget entity by ID
     */
    private Budget getBudgetEntity(Long id) {
        return budgetRespository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    /**
     * Gets the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Maps a Budget entity to a BudgetResponseDto
     */
    private BudgetResponseDto mapToDto(Budget budget) {
        BigDecimal spentAmount = budget.getSpentAmount() != null ? budget.getSpentAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);

        // Calculate percent used (0 to 100)
        double percentUsed = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentUsed = spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        return BudgetResponseDto.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .categoryType(budget.getCategory().getType().toString())
                .createdAt(budget.getCreatedAt())
                .modifiedAt(budget.getModifiedAt())
                .percentageUsed(percentUsed)
                .build();
    }


    /**
     * Gets all budgets for the current user
     */
    public List<BudgetResponseDto> getCurrentUserBudgets() {
        User currentUser = getCurrentUser();
        List<Budget> budgets = budgetRespository.findByUserId(currentUser.getId());
        return budgets.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets active budgets for the current user
     */
    public List<BudgetResponseDto> getCurrentUserActiveBudgets() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();

        List<Budget> activeBudgets = budgetRespository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        currentUser.getId(), today, today);

        return activeBudgets.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets budgets by category
     */
    public List<BudgetResponseDto> getBudgetsByCategory(Long categoryId) {
        User currentUser = getCurrentUser();
        List<Budget> budgets = budgetRespository.findByUserIdAndCategoryId(currentUser.getId(), categoryId);
        return budgets.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets budgets in a date range
     */
    public List<BudgetResponseDto> getBudgetsByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();

        // Find budgets that overlap with the given date range
        List<Budget> budgets = budgetRespository.findByUserId(currentUser.getId())
                .stream()
                .filter(budget ->
                        !budget.getEndDate().isBefore(startDate) && !budget.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());

        return budgets.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates a budget
     */
    @Transactional
    public BudgetResponseDto updateBudget(Long id, BudgetRequestDto requestDto) {
        Budget budget = getBudgetEntity(id);

        // Validate user owns this budget
        User currentUser = getCurrentUser();
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to update this budget");
        }

        // Validate dates
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Get Category
        Category category = null;
        if (requestDto.getCategoryId() != null) {
            category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
        }

        // Update budget entity
        budget.setAmount(requestDto.getAmount());
        budget.setStartDate(requestDto.getStartDate());
        budget.setEndDate(requestDto.getEndDate());

        if (category != null && !budget.getCategory().getId().equals(category.getId())) {
            // Category has changed, recalculate spent amount
            budget.setCategory(category);
            budget = budgetRespository.save(budget);
            updateBudgetSpentAmount(budget);
        } else {
            budget = budgetRespository.save(budget);
        }

        return mapToDto(budget);
    }
}
