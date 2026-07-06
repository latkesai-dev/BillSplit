package com.billsplit.controller;

import com.billsplit.dto.SettlementDtos.*;
import com.billsplit.entity.User;
import com.billsplit.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @GetMapping("/group/{groupId}/suggestions")
    public List<DebtSuggestion> suggestions(@PathVariable Long groupId) {
        return settlementService.minimizeDebts(groupId);
    }

    @PostMapping
    public SettlementResponse record(@AuthenticationPrincipal User user, @RequestBody SettleRequest req) {
        if(!user.getId().equals(req.fromUserId())){
            throw new IllegalArgumentException("Only the payer can mark a settlement as paid");
        }
        return settlementService.recordSettlement(req);
    }

    @GetMapping("/group/{groupId}")
    public List<SettlementResponse> history(@PathVariable Long groupId) {
        return settlementService.getGroupSettlements(groupId);
    }
}
