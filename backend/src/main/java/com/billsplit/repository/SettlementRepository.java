package com.billsplit.repository;
import com.billsplit.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupIdOrderBySettledAtDesc(Long groupId);
    // Total already paid from one user to another in this group
    @Query("""
        SELECT COALESCE(SUM(s.amount), 0)
        FROM Settlement s
        WHERE s.group.id = :groupId
        AND s.fromUser.id = :fromUserId
        AND s.toUser.id = :toUserId
        AND s.status = 'PAID'
    """)
    BigDecimal totalAlreadyPaid(Long groupId, Long fromUserId, Long toUserId);
}
