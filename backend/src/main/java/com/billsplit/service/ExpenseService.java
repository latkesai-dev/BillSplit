package com.billsplit.service;

import com.billsplit.aop.LogActivity;
import com.billsplit.dto.ExpenseDtos.*;
import com.billsplit.entity.*;
import com.billsplit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @LogActivity(action = "EXPENSE_ADDED")
    @Transactional
    public ExpenseResponse addExpense(User paidBy, AddExpenseRequest req) {
        Group group = groupRepository.findById(req.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        Expense expense = Expense.builder()
                .group(group).paidBy(paidBy)
                .description(req.description()).amount(req.amount())
                .build();
        expenseRepository.save(expense);

        // determine who to split among
        List<User> splitUsers;
        if (req.splitAmongUserIds() == null || req.splitAmongUserIds().isEmpty()) {
            // split equally among all group members
            splitUsers = groupMemberRepository.findByGroupId(group.getId())
                    .stream().map(GroupMember::getUser).toList();
        } else {
            splitUsers = userRepository.findAllById(req.splitAmongUserIds());
        }

        // equal split — last person gets the remainder to avoid rounding issues
        BigDecimal perPerson = req.amount()
                .divide(BigDecimal.valueOf(splitUsers.size()), 2, RoundingMode.FLOOR);
        BigDecimal remainder = req.amount().subtract(perPerson.multiply(BigDecimal.valueOf(splitUsers.size())));

        List<SplitDetail> splitDetails = new ArrayList<>();
        for (int i = 0; i < splitUsers.size(); i++) {
            User u = splitUsers.get(i);
            BigDecimal share = (i == splitUsers.size() - 1) ? perPerson.add(remainder) : perPerson;
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense).user(u).amountOwed(share).build();
            expenseSplitRepository.save(split);
            splitDetails.add(new SplitDetail(u.getId(), u.getFullName(), share));
        }

        return new ExpenseResponse(expense.getId(), expense.getDescription(),
                expense.getAmount(), paidBy.getFullName(), expense.getCreatedAt(), splitDetails);
    }

    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getDescription(), e.getAmount(),
                        e.getPaidBy().getFullName(), e.getCreatedAt(),
                        expenseSplitRepository.findByExpenseId(e.getId()).stream()
                                .map(s -> new SplitDetail(s.getUser().getId(),
                                        s.getUser().getFullName(), s.getAmountOwed()))
                                .toList()))
                .toList();
    }

    public List<BalanceSummary> getBalanceSummary(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(m -> {
                    User u = m.getUser();
                    BigDecimal paid = expenseSplitRepository.totalPaidByUserInGroup(groupId, u.getId());
                    BigDecimal owed = expenseSplitRepository.totalOwedByUserInGroup(groupId, u.getId());
                    return new BalanceSummary(u.getId(), u.getFullName(), paid, owed, paid.subtract(owed));
                }).toList();
    }
}
