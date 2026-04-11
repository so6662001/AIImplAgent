package com.aimpl.domain.videolibrary.vo;

import lombok.Data;

@Data
public class VideoClipMatchVO {

    private Long clipId;
    private Long videoId;
    private String videoTitle;
    private String clipTitle;
    private Integer startSecond;
    private Integer endSecond;
    private String relatedPage;
    private String relatedField;
    private String operationStep;
    private String videoUrl;
    private String matchType;
}
