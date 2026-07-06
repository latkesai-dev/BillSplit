package com.billsplit.service;

import com.billsplit.aop.LogActivity;
import com.billsplit.dto.ExpenseDtos.BalanceSummary;
import com.billsplit.dto.SettlementDtos.*;
import com.billsplit.entity.*;
import com.billsplit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    /**
     * DEBT MINIMIZATION ALGORITHM
     * ─────────────────────────────────────────────────────────────────────
     * Same approach used by Splitwise. Works in O(n log n).
     *
     * Step 1 — Compute net balance per person:
     *   netBalance = totalPaid - totalOwed
     *   positive → this person is owed money (creditor)
     *   negative → this person owes money   (debtor)
     *
     * Step 2 — Greedily match the biggest debtor with the biggest creditor:
     *   Take max creditor and max debtor.
     *   The smaller of |debtor| and |creditor| is the transaction amount.
     *   Reduce both balances by that amount.
     *   If a balance hits zero, remove that person from the heap.
     *   Repeat until all balances are zero.
     *
     * Result: minimum number of transactions to settle the group.
     * Example: 5 people, 10 raw debts → reduced to 4 transactions.
     * ─────────────────────────────────────────────────────────────────────
     */
    public List<DebtSuggestion> minimizeDebts(Long groupId) {
        List<BalanceSummary> balances = expenseService.getBalanceSummary(groupId);

        // max-heap for creditors (highest net balance first)
        PriorityQueue<BalanceSummary> creditors = new PriorityQueue<>(
                Comparator.comparing(BalanceSummary::netBalance).reversed());

        // min-heap for debtors (most negative first)
        PriorityQueue<BalanceSummary> debtors = new PriorityQueue<>(
                Comparator.comparing(BalanceSummary::netBalance));

        for (BalanceSummary b : balances) {
            if (b.netBalance().compareTo(BigDecimal.ZERO) > 0) creditors.offer(b);
            else if (b.netBalance().compareTo(BigDecimal.ZERO) < 0) debtors.offer(b);
            // zero balance → settled, skip
        }

        List<DebtSuggestion> suggestions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            BalanceSummary creditor = creditors.poll();
            BalanceSummary debtor   = debtors.poll();

            BigDecimal creditAmt = creditor.netBalance();
            BigDecimal debtAmt   = debtor.netBalance().negate(); // make positive

            BigDecimal transactionAmt = creditAmt.min(debtAmt).setScale(2, RoundingMode.HALF_UP);

            BigDecimal alreadyPaid = settlementRepository.totalAlreadyPaid(groupId, debtor.userId(), creditor.userId());
            BigDecimal remaining = transactionAmt.subtract(alreadyPaid).setScale(2,RoundingMode.HALF_UP);
            suggestions.add(new DebtSuggestion(
                    debtor.userId(),   debtor.userName(),
                    creditor.userId(), creditor.userName(),
                    remaining));

            BigDecimal remainingCredit = creditAmt.subtract(transactionAmt);
            BigDecimal remainingDebt   = debtAmt.subtract(transactionAmt);

            if (remainingCredit.compareTo(BigDecimal.valueOf(0.01)) > 0)
                creditors.offer(new BalanceSummary(creditor.userId(), creditor.userName(),
                        creditor.totalPaid(), creditor.totalOwed(), remainingCredit));

            if (remainingDebt.compareTo(BigDecimal.valueOf(0.01)) > 0)
                debtors.offer(new BalanceSummary(debtor.userId(), debtor.userName(),
                        debtor.totalPaid(), debtor.totalOwed(), remainingDebt.negate()));
        }

        return suggestions;
    }

    @LogActivity(action = "SETTLEMENT_RECORDED")
    public SettlementResponse recordSettlement(SettleRequest req) {
        Group group = groupRepository.findById(req.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User from = userRepository.findById(req.fromUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User to = userRepository.findById(req.toUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Settlement settlement = Settlement.builder()
                .group(group).fromUser(from).toUser(to).amount(req.amount()).build();
        settlementRepository.save(settlement);

        return new SettlementResponse(settlement.getId(),
                from.getFullName(), to.getFullName(),
                settlement.getAmount(), settlement.getSettledAt(),settlement.getStatus());
    }

    public List<SettlementResponse> getGroupSettlements(Long groupId) {
        return settlementRepository.findByGroupIdOrderBySettledAtDesc(groupId).stream()
                .map(s -> new SettlementResponse(s.getId(),
                        s.getFromUser().getFullName(), s.getToUser().getFullName(),
                        s.getAmount(), s.getSettledAt(),s.getStatus()))
                .toList();
    }

    /**
     * Spring Scheduling — runs every day at 9am.
     * Flags groups with unsettled debts older than 7 days by logging a reminder.
     * In a real app this would send an email/push notification.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void remindUnsettledDebts() {
        log.info("[SettlementReminder] Checking groups with pending debts older than 7 days...");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        groupRepository.findAll().forEach(group -> {
            List<DebtSuggestion> debts = minimizeDebts(group.getId());
            if (!debts.isEmpty()) {
                log.warn("[SettlementReminder] Group '{}' has {} unsettled debts",
                        group.getName(), debts.size());
            }
        });
    }
}
