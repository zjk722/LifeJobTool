package com.homeservice.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    CREATED("待支付"),
    PAID("已支付"),
    DISPATCHING("派单中"),
    ASSIGNED("已接单"),
    EN_ROUTE("已出发"),
    SERVING("服务中"),
    PENDING_CONFIRM("待确认"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    AFTER_SALE("售后中");

    private final String description;
}
