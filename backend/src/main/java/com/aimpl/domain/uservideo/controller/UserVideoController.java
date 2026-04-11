package com.aimpl.domain.uservideo.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.uservideo.dto.VideoApprovalDTO;
import com.aimpl.domain.uservideo.entity.UserVideo;
import com.aimpl.domain.uservideo.service.UserVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-videos")
@RequiredArgsConstructor
public class UserVideoController {

    private final UserVideoService userVideoService;

    @GetMapping
    public R<List<UserVideo>> listAll(@RequestParam(required = false) String approvalStatus) {
        return R.ok(userVideoService.listAllVideos(approvalStatus));
    }

    @PutMapping("/{id}/approve")
    public R<UserVideo> approve(@PathVariable Long id,
                                @Valid @RequestBody VideoApprovalDTO dto,
                                @RequestParam(defaultValue = "admin") String approvedBy) {
        return R.ok(userVideoService.approve(id, dto, approvedBy));
    }

    @GetMapping("/pending-count")
    public R<Long> pendingCount() {
        return R.ok(userVideoService.countPending());
    }
}
