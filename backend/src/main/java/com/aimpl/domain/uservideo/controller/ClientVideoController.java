package com.aimpl.domain.uservideo.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.uservideo.dto.UserVideoPublishDTO;
import com.aimpl.domain.uservideo.dto.VideoLearningDTO;
import com.aimpl.domain.uservideo.entity.PointsTransaction;
import com.aimpl.domain.uservideo.entity.UserVideo;
import com.aimpl.domain.uservideo.entity.UserVideoLearning;
import com.aimpl.domain.uservideo.service.PointsService;
import com.aimpl.domain.uservideo.service.UserVideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/videos")
@RequiredArgsConstructor
public class ClientVideoController {

    private final UserVideoService userVideoService;
    private final PointsService pointsService;

    @PostMapping("/publish")
    public R<UserVideo> publish(@Valid @RequestBody UserVideoPublishDTO dto,
                                HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(userVideoService.publish(clientUserId, dto));
    }

    @GetMapping("/approved")
    public R<List<UserVideo>> listApproved(@RequestParam(required = false) String categoryModule) {
        return R.ok(userVideoService.listApprovedVideos(categoryModule));
    }

    @GetMapping("/my-published")
    public R<List<UserVideo>> myPublished(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(userVideoService.listMyPublished(clientUserId));
    }

    @PostMapping("/{videoId}/start-learning")
    public R<UserVideoLearning> startLearning(@PathVariable Long videoId,
                                               HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(userVideoService.startLearning(clientUserId, videoId));
    }

    @PostMapping("/learning-progress")
    public R<UserVideoLearning> updateLearningProgress(@Valid @RequestBody VideoLearningDTO dto,
                                                        HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(userVideoService.updateProgress(clientUserId, dto));
    }

    @GetMapping("/my-learning")
    public R<List<UserVideoLearning>> myLearning(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(userVideoService.listMyLearning(clientUserId));
    }

    @GetMapping("/points/balance")
    public R<Integer> pointsBalance(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(pointsService.getBalance(clientUserId));
    }

    @GetMapping("/points/history")
    public R<List<PointsTransaction>> pointsHistory(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(pointsService.getHistory(clientUserId));
    }
}
