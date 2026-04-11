package com.aimpl.domain.accountset.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.accountset.dto.AccountSetCreateDTO;
import com.aimpl.domain.accountset.entity.AccountSet;
import com.aimpl.domain.accountset.service.AccountSetRecommendService;
import com.aimpl.domain.accountset.service.AccountSetService;
import com.aimpl.domain.accountset.vo.AccountSetRecommendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-sets")
@RequiredArgsConstructor
public class AccountSetController {

    private final AccountSetService accountSetService;
    private final AccountSetRecommendService accountSetRecommendService;

    @PostMapping
    public R<AccountSet> create(@Valid @RequestBody AccountSetCreateDTO dto) {
        return R.ok(accountSetService.create(dto));
    }

    @GetMapping("/{id}")
    public R<AccountSet> get(@PathVariable Long id) {
        return R.ok(accountSetService.getById(id));
    }

    @GetMapping
    public R<List<AccountSet>> list(@RequestParam Long projectId) {
        return R.ok(accountSetService.listByProjectId(projectId));
    }

    @PutMapping("/{id}/activate")
    public R<AccountSet> activate(@PathVariable Long id) {
        return R.ok(accountSetService.activate(id));
    }

    @GetMapping("/recommend/{projectId}")
    public R<AccountSetRecommendVO> recommend(@PathVariable Long projectId) {
        return R.ok(accountSetRecommendService.recommend(projectId));
    }

    @PostMapping("/create-from-recommendation/{projectId}")
    public R<AccountSet> createFromRecommendation(@PathVariable Long projectId) {
        return R.ok(accountSetRecommendService.createFromRecommendation(projectId));
    }
}
