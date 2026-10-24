package com.homeservice.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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
@TableName("service_order")
public class ServiceOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long workerId;

    private Long addressId;

    private LocalDateTime appointmentStart;

    private LocalDateTime appointmentEnd;

    private OrderStatus status;

    private String dispatchMode;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal commissionRate;

    private String cancelReason;

    private String remark;

    @Version
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime assignedAt;

    private LocalDateTime finishedAt;
}
