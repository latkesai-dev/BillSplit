package com.billsplit.repository;
import com.billsplit.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByExpenseId(Long expenseId);

    @Query("""
        SELECT es FROM ExpenseSplit es
        JOIN es.expense e
        WHERE e.group.id = :groupId
    """)
    List<ExpenseSplit> findByGroupId(Long groupId);

    @Query("""
        SELECT COALESCE(SUM(es.amountOwed), 0)
        FROM ExpenseSplit es
        JOIN es.expense e
        WHERE e.group.id = :groupId AND es.user.id = :userId
    """)
    BigDecimal totalOwedByUserInGroup(Long groupId, Long userId);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.group.id = :groupId AND e.paidBy.id = :userId
    """)
    BigDecimal totalPaidByUserInGroup(Long groupId, Long userId);
}
