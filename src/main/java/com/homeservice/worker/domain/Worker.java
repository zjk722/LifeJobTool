package com.homeservice.worker.domain;

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
@TableName("worker")
public class Worker {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    private WorkerStatus status;

    private Integer creditScore;

    private BigDecimal rating;

    private Integer orderCount;

    private Integer serviceRadius;

    private BigDecimal homeLat;

    private BigDecimal homeLng;

    private LocalDateTime createdAt;
}
