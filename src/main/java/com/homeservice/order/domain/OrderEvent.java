package com.homeservice.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderEvent {

    PAY_SUCCESS("支付成功"),
    START_DISPATCH("开始派单"),
    WORKER_ACCEPT("服务人员接单"),
    WORKER_REJECT("服务人员拒单"),
    WORKER_DEPART("服务人员出发"),
    CHECK_IN("到达打卡"),
    SUBMIT_COMPLETE("提交完成"),
    USER_CONFIRM("用户确认"),
    AUTO_CONFIRM("超时自动确认"),
    USER_CANCEL("用户取消"),
    RESCHEDULE("用户改约"),
    USER_COMPLAINT("用户投诉"),
    ARBITRATION_COMPLETE("仲裁完成-放款"),
    ARBITRATION_REFUND("仲裁完成-退款");

    private final String description;
}
