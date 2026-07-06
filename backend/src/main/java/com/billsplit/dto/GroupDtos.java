package com.billsplit.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class GroupDtos {
    public record CreateGroupRequest(@NotBlank String name, String description) {}
    public record AddMemberRequest(@NotBlank @Email String email) {}

    public record GroupResponse(Long id, String name, String description,
                                 String createdByName, LocalDateTime createdAt,
                                 int memberCount) {}

    public record MemberResponse(Long userId, String fullName, String email) {}
}
