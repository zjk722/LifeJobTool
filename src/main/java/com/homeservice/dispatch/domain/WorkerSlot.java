package com.homeservice.dispatch.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("worker_slot")
public class WorkerSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workerId;

    private LocalDateTime slotStart;

    private LocalDateTime slotEnd;

    private SlotStatus status;

    private Long orderId;

    @Version
    private Integer version;
}
