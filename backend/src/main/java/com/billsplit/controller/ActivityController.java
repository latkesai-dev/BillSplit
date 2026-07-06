package com.billsplit.controller;

import com.billsplit.entity.ActivityLog;
import com.billsplit.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/recent")
    public List<ActivityLog> recent() {
        return activityLogRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();
    }
}
