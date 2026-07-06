package com.billsplit.repository;
import com.billsplit.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
}
