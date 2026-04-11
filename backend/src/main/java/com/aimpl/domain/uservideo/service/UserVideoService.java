package com.aimpl.domain.uservideo.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.mapper.ClientUserMapper;
import com.aimpl.domain.uservideo.dto.UserVideoPublishDTO;
import com.aimpl.domain.uservideo.dto.VideoApprovalDTO;
import com.aimpl.domain.uservideo.dto.VideoLearningDTO;
import com.aimpl.domain.uservideo.entity.UserVideo;
import com.aimpl.domain.uservideo.entity.UserVideoLearning;
import com.aimpl.domain.uservideo.mapper.UserVideoLearningMapper;
import com.aimpl.domain.uservideo.mapper.UserVideoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserVideoService extends ServiceImpl<UserVideoMapper, UserVideo> {

    private final UserVideoLearningMapper learningMapper;
    private final ClientUserMapper clientUserMapper;
    private final PointsService pointsService;

    @Transactional
    public UserVideo publish(Long publisherId, UserVideoPublishDTO dto) {
        ClientUser publisher = clientUserMapper.selectById(publisherId);
        if (publisher == null) {
            throw new BizException("发布者不存在");
        }

        UserVideo video = new UserVideo();
        video.setPublisherId(publisherId);
        video.setPublisherName(publisher.getEmployeeName());
        video.setProjectId(publisher.getProjectId());
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setCategoryModule(dto.getCategoryModule());
        video.setVideoUrl(dto.getVideoUrl());
        video.setVideoDuration(dto.getVideoDuration());
        video.setThumbnailUrl(dto.getThumbnailUrl());
        video.setPointsCost(dto.getPointsCost() != null ? dto.getPointsCost() : 0);
        video.setTotalViews(0);
        video.setTotalLearners(0);
        video.setPublisherEarnedPoints(0);
        video.setApprovalStatus("PENDING");
        video.setEnabled(true);
        save(video);

        pointsService.processPoints(publisherId, 10, "发布视频系统奖励: " + dto.getTitle(), video.getId());

        return video;
    }

    @Transactional
    public UserVideo approve(Long videoId, VideoApprovalDTO dto, String approvedBy) {
        UserVideo video = getById(videoId);
        if (video == null) {
            throw new BizException("视频不存在: " + videoId);
        }

        if (Boolean.TRUE.equals(dto.getApproved())) {
            video.setApprovalStatus("APPROVED");
            video.setApprovedBy(approvedBy);
            video.setApprovedAt(LocalDateTime.now());
        } else {
            video.setApprovalStatus("REJECTED");
            video.setRejectionReason(dto.getRejectionReason());
        }
        updateById(video);
        return video;
    }

    @Transactional
    public UserVideoLearning startLearning(Long learnerId, Long videoId) {
        UserVideo video = getById(videoId);
        if (video == null) {
            throw new BizException("视频不存在: " + videoId);
        }
        if (!"APPROVED".equals(video.getApprovalStatus())) {
            throw new BizException("视频未审核通过，无法学习");
        }

        UserVideoLearning existing = learningMapper.selectOne(
                new LambdaQueryWrapper<UserVideoLearning>()
                        .eq(UserVideoLearning::getVideoId, videoId)
                        .eq(UserVideoLearning::getLearnerId, learnerId));
        if (existing != null) {
            return existing;
        }

        ClientUser learner = clientUserMapper.selectById(learnerId);
        if (learner == null) {
            throw new BizException("学习者不存在");
        }

        int cost = video.getPointsCost() != null ? video.getPointsCost() : 0;

        if (cost > 0) {
            pointsService.processPoints(learnerId, -cost,
                    "观看视频消费积分: " + video.getTitle(), videoId);

            pointsService.processPoints(video.getPublisherId(), cost,
                    "视频被观看获得积分: " + video.getTitle(), videoId);

            video.setPublisherEarnedPoints(
                    (video.getPublisherEarnedPoints() != null ? video.getPublisherEarnedPoints() : 0) + cost);
        }

        video.setTotalLearners((video.getTotalLearners() != null ? video.getTotalLearners() : 0) + 1);
        updateById(video);

        UserVideoLearning learning = new UserVideoLearning();
        learning.setVideoId(videoId);
        learning.setLearnerId(learnerId);
        learning.setLearnerName(learner.getEmployeeName());
        learning.setWatchedDuration(0);
        learning.setCompleted(false);
        learning.setPointsPaid(cost);
        learningMapper.insert(learning);

        return learning;
    }

    @Transactional
    public UserVideoLearning updateProgress(Long learnerId, VideoLearningDTO dto) {
        UserVideoLearning learning = learningMapper.selectOne(
                new LambdaQueryWrapper<UserVideoLearning>()
                        .eq(UserVideoLearning::getVideoId, dto.getVideoId())
                        .eq(UserVideoLearning::getLearnerId, learnerId));

        if (learning == null) {
            throw new BizException("尚未开始学习该视频");
        }

        learning.setWatchedDuration(dto.getWatchedDuration());

        UserVideo video = getById(dto.getVideoId());
        if (video != null && video.getVideoDuration() != null && video.getVideoDuration() > 0) {
            double ratio = (double) dto.getWatchedDuration() / video.getVideoDuration();
            if (ratio >= 0.9 && !Boolean.TRUE.equals(learning.getCompleted())) {
                learning.setCompleted(true);
                learning.setCompletedAt(LocalDateTime.now());
            }
        }

        learningMapper.updateById(learning);
        return learning;
    }

    public List<UserVideo> listApprovedVideos(String categoryModule) {
        LambdaQueryWrapper<UserVideo> wrapper = new LambdaQueryWrapper<UserVideo>()
                .eq(UserVideo::getApprovalStatus, "APPROVED")
                .eq(UserVideo::getEnabled, true)
                .orderByDesc(UserVideo::getCreateTime);
        if (categoryModule != null && !categoryModule.isBlank()) {
            wrapper.eq(UserVideo::getCategoryModule, categoryModule);
        }
        return list(wrapper);
    }

    public List<UserVideo> listAllVideos(String approvalStatus) {
        LambdaQueryWrapper<UserVideo> wrapper = new LambdaQueryWrapper<UserVideo>()
                .orderByDesc(UserVideo::getCreateTime);
        if (approvalStatus != null && !approvalStatus.isBlank()) {
            wrapper.eq(UserVideo::getApprovalStatus, approvalStatus);
        }
        return list(wrapper);
    }

    public List<UserVideo> listMyPublished(Long publisherId) {
        return list(new LambdaQueryWrapper<UserVideo>()
                .eq(UserVideo::getPublisherId, publisherId)
                .orderByDesc(UserVideo::getCreateTime));
    }

    public List<UserVideoLearning> listMyLearning(Long learnerId) {
        return learningMapper.selectList(
                new LambdaQueryWrapper<UserVideoLearning>()
                        .eq(UserVideoLearning::getLearnerId, learnerId)
                        .orderByDesc(UserVideoLearning::getCreateTime));
    }

    public long countPending() {
        return count(new LambdaQueryWrapper<UserVideo>()
                .eq(UserVideo::getApprovalStatus, "PENDING"));
    }
}
