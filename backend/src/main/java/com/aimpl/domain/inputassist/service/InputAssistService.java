package com.aimpl.domain.inputassist.service;

import com.aimpl.domain.inputassist.dto.InputAssistLogDTO;
import com.aimpl.domain.inputassist.entity.InputAssistLog;
import com.aimpl.domain.inputassist.mapper.InputAssistLogMapper;
import com.aimpl.domain.inputassist.vo.InputAssistStatsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InputAssistService extends ServiceImpl<InputAssistLogMapper, InputAssistLog> {

    @Transactional
    public InputAssistLog logAssist(InputAssistLogDTO dto) {
        InputAssistLog log = new InputAssistLog();
        log.setUserId(dto.getUserId());
        log.setPage(dto.getPage());
        log.setField(dto.getField());
        log.setTriggerType(dto.getTriggerType());
        log.setQuestion(dto.getQuestion());
        log.setAnswer(dto.getAnswer());
        log.setVideoClipId(dto.getVideoClipId());
        log.setVideoPlayed(dto.getVideoPlayed() != null ? dto.getVideoPlayed() : false);
        log.setResolved(dto.getResolved() != null ? dto.getResolved() : false);
        save(log);
        return log;
    }

    public InputAssistStatsVO getStats(String page) {
        List<InputAssistLog> logs = list(
                new LambdaQueryWrapper<InputAssistLog>()
                        .eq(InputAssistLog::getPage, page));

        InputAssistStatsVO stats = new InputAssistStatsVO();
        stats.setTotalAssists(logs.size());

        Map<String, Long> byTrigger = logs.stream()
                .filter(l -> l.getTriggerType() != null)
                .collect(Collectors.groupingBy(InputAssistLog::getTriggerType, Collectors.counting()));
        stats.setByTriggerType(byTrigger);

        long resolvedCount = logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.getResolved()))
                .count();
        stats.setResolvedRate(logs.isEmpty() ? 0.0 : (double) resolvedCount / logs.size());

        Map<String, Long> fieldCounts = logs.stream()
                .filter(l -> l.getField() != null && !l.getField().isBlank())
                .collect(Collectors.groupingBy(InputAssistLog::getField, Collectors.counting()));
        List<InputAssistStatsVO.FieldCount> topFields = fieldCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new InputAssistStatsVO.FieldCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        stats.setTopFields(topFields);

        return stats;
    }

    public List<InputAssistLog> listByPage(String page) {
        return list(new LambdaQueryWrapper<InputAssistLog>()
                .eq(InputAssistLog::getPage, page)
                .orderByDesc(InputAssistLog::getCreateTime)
                .last("LIMIT 100"));
    }
}
