package com.homeservice.dispatch.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dispatch_task")
public class DispatchTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long workerId;

    private BigDecimal score;

    private DispatchTaskStatus status;

    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}
