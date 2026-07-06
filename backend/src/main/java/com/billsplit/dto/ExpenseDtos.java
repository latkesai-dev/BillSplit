package com.billsplit.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseDtos {
    public record AddExpenseRequest(
            @NotBlank String description,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotNull Long groupId,
            List<Long> splitAmongUserIds  // null = split equally among all
    ) {}

    public record ExpenseResponse(Long id, String description, BigDecimal amount,
                                   String paidByName, LocalDateTime createdAt,
                                   List<SplitDetail> splits) {}

    public record SplitDetail(Long userId, String userName, BigDecimal amountOwed) {}

    public record BalanceSummary(Long userId, String userName,
                                  BigDecimal totalPaid, BigDecimal totalOwed,
                                  BigDecimal netBalance) {} // positive = owed money, negative = owes money
}
