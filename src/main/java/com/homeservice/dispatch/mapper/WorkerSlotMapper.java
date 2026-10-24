package com.homeservice.dispatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeservice.dispatch.domain.WorkerSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface WorkerSlotMapper extends BaseMapper<WorkerSlot> {

    /**
     * 原子占用一段时间内的所有空闲槽位。
     *
     * <p>返回影响行数；调用方必须校验它是否等于所需槽数，
     * 不相等说明并发下被抢走了一部分，事务需要回滚。</p>
     */
    @Update("""
            UPDATE worker_slot
               SET status = 'BOOKED',
                   order_id = #{orderId},
                   version = version + 1
             WHERE worker_id = #{workerId}
               AND slot_start >= #{start}
               AND slot_start < #{end}
               AND status = 'FREE'
            """)
    int bookSlots(@Param("workerId") Long workerId,
                  @Param("start") LocalDateTime start,
                  @Param("end") LocalDateTime end,
                  @Param("orderId") Long orderId);
}
