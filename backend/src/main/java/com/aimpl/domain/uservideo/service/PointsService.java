package com.aimpl.domain.uservideo.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.mapper.ClientUserMapper;
import com.aimpl.domain.uservideo.entity.PointsTransaction;
import com.aimpl.domain.uservideo.mapper.PointsTransactionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final ClientUserMapper clientUserMapper;
    private final PointsTransactionMapper pointsTransactionMapper;

    /**
     * Call external points API to credit/debit points.
     * Currently a mock implementation — structure ready for real API integration.
     *
     * @param userId client user ID
     * @param amount positive = credit (earn), negative = debit (spend)
     * @param reason description of the transaction
     * @param relatedVideoId associated video ID (nullable)
     * @return transaction record
     */
    @Transactional
    public PointsTransaction processPoints(Long userId, int amount, String reason, Long relatedVideoId) {
        ClientUser user = clientUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        int balanceBefore = user.getPoints() != null ? user.getPoints() : 0;

        if (amount < 0 && balanceBefore + amount < 0) {
            throw new BizException("积分不足，当前积分: " + balanceBefore + "，需要: " + Math.abs(amount));
        }

        int balanceAfter = balanceBefore + amount;

        // ===== External API Call (Mock) =====
        boolean apiCalled = true;
        String apiResponse = "{\"success\":true,\"balance\":" + balanceAfter + "}";
        // ===== End Mock =====

        user.setPoints(balanceAfter);
        clientUserMapper.updateById(user);

        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(userId);
        tx.setUserName(user.getEmployeeName());
        tx.setTransactionType(determineTransactionType(amount, reason));
        tx.setAmount(amount);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setRelatedVideoId(relatedVideoId);
        tx.setDescription(reason);
        tx.setExternalApiCalled(apiCalled);
        tx.setExternalApiResponse(apiResponse);
        pointsTransactionMapper.insert(tx);

        return tx;
    }

    private String determineTransactionType(int amount, String reason) {
        if (reason != null && reason.contains("系统奖励")) {
            return "SYSTEM_REWARD";
        }
        if (reason != null && reason.contains("管理员")) {
            return "ADMIN_ADJUST";
        }
        return amount > 0 ? "EARN_FROM_VIDEO" : "SPEND_ON_VIDEO";
    }

    public int getBalance(Long userId) {
        ClientUser user = clientUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user.getPoints() != null ? user.getPoints() : 0;
    }

    public List<PointsTransaction> getHistory(Long userId) {
        return pointsTransactionMapper.selectList(
                new LambdaQueryWrapper<PointsTransaction>()
                        .eq(PointsTransaction::getUserId, userId)
                        .orderByDesc(PointsTransaction::getCreateTime));
    }
}
