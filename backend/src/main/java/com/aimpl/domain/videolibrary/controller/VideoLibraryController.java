package com.aimpl.domain.videolibrary.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.videolibrary.dto.VideoClipCreateDTO;
import com.aimpl.domain.videolibrary.dto.VideoResourceCreateDTO;
import com.aimpl.domain.videolibrary.entity.VideoClip;
import com.aimpl.domain.videolibrary.entity.VideoResource;
import com.aimpl.domain.videolibrary.service.VideoLibraryService;
import com.aimpl.domain.videolibrary.vo.VideoClipMatchVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-library")
@RequiredArgsConstructor
public class VideoLibraryController {

    private final VideoLibraryService videoLibraryService;

    @PostMapping("/videos")
    public R<VideoResource> createVideo(@Valid @RequestBody VideoResourceCreateDTO dto) {
        return R.ok(videoLibraryService.createVideo(dto));
    }

    @GetMapping("/videos")
    public R<List<VideoResource>> listVideos(@RequestParam(required = false) String module) {
        return R.ok(videoLibraryService.listVideos(module));
    }

    @PostMapping("/videos/{videoId}/clips")
    public R<VideoClip> createClip(@PathVariable Long videoId,
                                   @Valid @RequestBody VideoClipCreateDTO dto) {
        dto.setVideoId(videoId);
        return R.ok(videoLibraryService.createClip(videoId, dto));
    }

    @GetMapping("/videos/{videoId}/clips")
    public R<List<VideoClip>> listClips(@PathVariable Long videoId) {
        return R.ok(videoLibraryService.listClips(videoId));
    }

    @GetMapping("/clips/match")
    public R<List<VideoClipMatchVO>> matchClips(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String question) {
        return R.ok(videoLibraryService.matchClips(page, field, question));
    }
}
