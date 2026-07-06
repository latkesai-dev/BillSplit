package com.billsplit.controller;

import com.billsplit.dto.GroupDtos.*;
import com.billsplit.entity.User;
import com.billsplit.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public GroupResponse create(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody CreateGroupRequest req) {
        return groupService.createGroup(user, req);
    }

    @PostMapping("/{groupId}/members")
    public void addMember(@PathVariable Long groupId,
                           @Valid @RequestBody AddMemberRequest req) {
        groupService.addMember(groupId, req);
    }

    @GetMapping
    public List<GroupResponse> myGroups(@AuthenticationPrincipal User user) {
        return groupService.myGroups(user.getId());
    }

    @GetMapping("/{groupId}/members")
    public List<MemberResponse> members(@PathVariable Long groupId) {
        return groupService.getMembers(groupId);
    }
}
