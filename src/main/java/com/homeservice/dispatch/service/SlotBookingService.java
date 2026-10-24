package com.homeservice.dispatch.service;

import com.homeservice.common.exception.BizException;
import com.homeservice.common.exception.ErrorCode;
import com.homeservice.dispatch.mapper.WorkerSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SlotBookingService {

    public static final int SLOT_MINUTES = 30;

    private final WorkerSlotMapper workerSlotMapper;

    /**
     * 占用服务人员的连续时间槽。
     *
     * <p>bookSlots 是单条 UPDATE，并发下由数据库行锁 + 唯一索引兜底；
     * 影响行数不足时抛异常，让整个事务回滚，避免「占了一半」。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void book(Long workerId, LocalDateTime start, LocalDateTime end, Long orderId) {
        int required = requiredSlots(start, end);
        int affected = workerSlotMapper.bookSlots(workerId, start, end, orderId);
        if (affected != required) {
            throw new BizException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "需要 " + required + " 个时间槽，实际可用 " + affected);
        }
    }

    public int requiredSlots(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes <= 0 || minutes % SLOT_MINUTES != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "服务时长必须是 " + SLOT_MINUTES + " 分钟的整数倍");
        }
        return (int) (minutes / SLOT_MINUTES);
    }
}
