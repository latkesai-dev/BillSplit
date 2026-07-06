package com.billsplit.repository;
import com.billsplit.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
