package com.billsplit.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementDtos {
    public record DebtSuggestion(Long fromUserId, String fromUserName,
                                  Long toUserId, String toUserName,
                                  BigDecimal amount) {}

    public record SettleRequest(Long groupId, Long fromUserId, Long toUserId,
                                 BigDecimal amount) {}

    public record SettlementResponse(Long id, String fromUserName, String toUserName,
                                      BigDecimal amount, LocalDateTime settledAt,String status) {}
}
