package com.billsplit.controller;

import com.billsplit.dto.ExpenseDtos.*;
import com.billsplit.entity.User;
import com.billsplit.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse add(@AuthenticationPrincipal User user,
                                @Valid @RequestBody AddExpenseRequest req) {
        return expenseService.addExpense(user, req);
    }

    @GetMapping("/group/{groupId}")
    public List<ExpenseResponse> byGroup(@PathVariable Long groupId) {
        return expenseService.getGroupExpenses(groupId);
    }

    @GetMapping("/group/{groupId}/balances")
    public List<BalanceSummary> balances(@PathVariable Long groupId) {
        return expenseService.getBalanceSummary(groupId);
    }
}
