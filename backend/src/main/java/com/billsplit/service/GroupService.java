package com.billsplit.service;

import com.billsplit.aop.LogActivity;
import com.billsplit.dto.GroupDtos.*;
import com.billsplit.entity.*;
import com.billsplit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @LogActivity(action = "GROUP_CREATED")
    public GroupResponse createGroup(User creator, CreateGroupRequest req) {
        Group group = Group.builder()
                .name(req.name())
                .description(req.description())
                .createdBy(creator)
                .build();
        groupRepository.save(group);

        // auto-add creator as member
        GroupMember member = GroupMember.builder()
                .group(group).user(creator).build();
        groupMemberRepository.save(member);

        return toResponse(group, 1);
    }

    @LogActivity(action = "MEMBER_ADDED")
    public void addMember(Long groupId, AddMemberRequest req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId()))
            throw new IllegalArgumentException("User already in group");
        groupMemberRepository.save(GroupMember.builder().group(group).user(user).build());
    }

    public List<GroupResponse> myGroups(Long userId) {
        return groupRepository.findGroupsByUserId(userId).stream()
                .map(g -> toResponse(g, g.getMembers().size()))
                .toList();
    }

    public List<MemberResponse> getMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(m -> new MemberResponse(m.getUser().getId(),
                        m.getUser().getFullName(), m.getUser().getEmail()))
                .toList();
    }

    private GroupResponse toResponse(Group g, int memberCount) {
        return new GroupResponse(g.getId(), g.getName(), g.getDescription(),
                g.getCreatedBy().getFullName(), g.getCreatedAt(), memberCount);
    }
}
