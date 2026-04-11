package com.aimpl.domain.videolibrary.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.videolibrary.dto.VideoClipCreateDTO;
import com.aimpl.domain.videolibrary.dto.VideoResourceCreateDTO;
import com.aimpl.domain.videolibrary.entity.VideoClip;
import com.aimpl.domain.videolibrary.entity.VideoResource;
import com.aimpl.domain.videolibrary.mapper.VideoClipMapper;
import com.aimpl.domain.videolibrary.mapper.VideoResourceMapper;
import com.aimpl.domain.videolibrary.vo.VideoClipMatchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoLibraryService extends ServiceImpl<VideoResourceMapper, VideoResource> {

    private final VideoClipMapper clipMapper;

    @Transactional
    public VideoResource createVideo(VideoResourceCreateDTO dto) {
        VideoResource video = new VideoResource();
        video.setModule(dto.getModule());
        video.setFunctionName(dto.getFunctionName());
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setVideoUrl(dto.getVideoUrl());
        video.setDuration(dto.getDuration());
        video.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        video.setEnabled(true);
        save(video);
        return video;
    }

    public List<VideoResource> listVideos(String module) {
        LambdaQueryWrapper<VideoResource> qw = new LambdaQueryWrapper<>();
        if (module != null && !module.isBlank()) {
            qw.eq(VideoResource::getModule, module);
        }
        qw.eq(VideoResource::getEnabled, true);
        qw.orderByAsc(VideoResource::getSortOrder);
        return list(qw);
    }

    @Transactional
    public VideoClip createClip(Long videoId, VideoClipCreateDTO dto) {
        VideoResource video = getById(videoId);
        if (video == null) {
            throw new BizException("视频资源不存在: " + videoId);
        }

        VideoClip clip = new VideoClip();
        clip.setVideoId(videoId);
        clip.setClipTitle(dto.getClipTitle());
        clip.setStartSecond(dto.getStartSecond());
        clip.setEndSecond(dto.getEndSecond());
        clip.setRelatedPage(dto.getRelatedPage());
        clip.setRelatedField(dto.getRelatedField());
        clip.setOperationStep(dto.getOperationStep());
        clip.setSubtitleText(dto.getSubtitleText());
        clip.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        clipMapper.insert(clip);
        return clip;
    }

    public List<VideoClip> listClips(Long videoId) {
        return clipMapper.selectList(
                new LambdaQueryWrapper<VideoClip>()
                        .eq(VideoClip::getVideoId, videoId)
                        .orderByAsc(VideoClip::getSortOrder));
    }

    public List<VideoClipMatchVO> matchClips(String page, String field, String question) {
        Map<Long, VideoClipMatchVO> resultMap = new LinkedHashMap<>();

        if (page != null && !page.isBlank() && field != null && !field.isBlank()) {
            List<VideoClip> exactClips = clipMapper.selectList(
                    new LambdaQueryWrapper<VideoClip>()
                            .eq(VideoClip::getRelatedPage, page)
                            .eq(VideoClip::getRelatedField, field));
            for (VideoClip clip : exactClips) {
                resultMap.put(clip.getId(), toMatchVO(clip, "EXACT"));
            }
        }

        if (page != null && !page.isBlank()) {
            List<VideoClip> pageClips = clipMapper.selectList(
                    new LambdaQueryWrapper<VideoClip>()
                            .eq(VideoClip::getRelatedPage, page));
            for (VideoClip clip : pageClips) {
                resultMap.putIfAbsent(clip.getId(), toMatchVO(clip, "PAGE"));
            }
        }

        if (question != null && !question.isBlank()) {
            List<String> keywords = extractKeywords(question);
            if (!keywords.isEmpty()) {
                LambdaQueryWrapper<VideoClip> kwQuery = new LambdaQueryWrapper<>();
                kwQuery.and(wrapper -> {
                    for (String kw : keywords) {
                        wrapper.or(w -> w
                                .like(VideoClip::getClipTitle, kw)
                                .or().like(VideoClip::getSubtitleText, kw)
                                .or().like(VideoClip::getOperationStep, kw));
                    }
                });
                List<VideoClip> kwClips = clipMapper.selectList(kwQuery);
                for (VideoClip clip : kwClips) {
                    resultMap.putIfAbsent(clip.getId(), toMatchVO(clip, "KEYWORD"));
                }
            }
        }

        List<VideoClipMatchVO> results = new ArrayList<>(resultMap.values());
        fillVideoInfo(results);
        return results;
    }

    private VideoClipMatchVO toMatchVO(VideoClip clip, String matchType) {
        VideoClipMatchVO vo = new VideoClipMatchVO();
        vo.setClipId(clip.getId());
        vo.setVideoId(clip.getVideoId());
        vo.setClipTitle(clip.getClipTitle());
        vo.setStartSecond(clip.getStartSecond());
        vo.setEndSecond(clip.getEndSecond());
        vo.setRelatedPage(clip.getRelatedPage());
        vo.setRelatedField(clip.getRelatedField());
        vo.setOperationStep(clip.getOperationStep());
        vo.setMatchType(matchType);
        return vo;
    }

    private void fillVideoInfo(List<VideoClipMatchVO> clips) {
        Set<Long> videoIds = clips.stream()
                .map(VideoClipMatchVO::getVideoId)
                .collect(Collectors.toSet());
        if (videoIds.isEmpty()) return;

        Map<Long, VideoResource> videoMap = listByIds(videoIds).stream()
                .collect(Collectors.toMap(VideoResource::getId, v -> v));

        for (VideoClipMatchVO vo : clips) {
            VideoResource video = videoMap.get(vo.getVideoId());
            if (video != null) {
                vo.setVideoTitle(video.getTitle());
                vo.setVideoUrl(video.getVideoUrl());
            }
        }
    }

    private List<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        String[] stopWords = {"的", "了", "吗", "呢", "是", "在", "有", "和", "与",
                "我", "你", "他", "她", "它", "怎么", "如何", "什么", "请问", "一下"};
        Set<String> stopSet = new HashSet<>(Arrays.asList(stopWords));
        List<String> keywords = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                if (!sb.isEmpty()) {
                    String w = sb.toString();
                    if (!stopSet.contains(w) && w.length() >= 2) {
                        keywords.add(w);
                    }
                    sb.setLength(0);
                }
                String single = String.valueOf(c);
                if (!stopSet.contains(single) && !Character.isWhitespace(c)) {
                    // For Chinese, extract 2-char grams
                }
            }
        }
        if (!sb.isEmpty()) {
            String w = sb.toString();
            if (!stopSet.contains(w) && w.length() >= 2) {
                keywords.add(w);
            }
        }

        if (keywords.isEmpty() && text.length() >= 2) {
            for (int i = 0; i < text.length() - 1; i++) {
                String bigram = text.substring(i, i + 2);
                if (!bigram.isBlank() && !stopSet.contains(bigram)) {
                    keywords.add(bigram);
                    if (keywords.size() >= 5) break;
                }
            }
        }
        return keywords;
    }
}
