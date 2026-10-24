package com.homeservice.order.statemachine;

import com.homeservice.common.exception.BizException;
import com.homeservice.common.exception.ErrorCode;
import com.homeservice.order.domain.OrderEvent;
import com.homeservice.order.domain.OrderStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 订单状态机。
 *
 * <p>所有状态流转集中在这里，业务代码不允许直接 setStatus。
 * 新增流转时只改 TRANSITIONS，并补一条单测。</p>
 */
public final class OrderStateMachine {

    private static final List<OrderTransition> TRANSITIONS = List.of(
            new OrderTransition(OrderStatus.CREATED, OrderEvent.PAY_SUCCESS, OrderStatus.PAID),
            new OrderTransition(OrderStatus.CREATED, OrderEvent.USER_CANCEL, OrderStatus.CANCELLED),
            new OrderTransition(OrderStatus.PAID, OrderEvent.START_DISPATCH, OrderStatus.DISPATCHING),
            new OrderTransition(OrderStatus.PAID, OrderEvent.USER_CANCEL, OrderStatus.CANCELLED),
            new OrderTransition(OrderStatus.DISPATCHING, OrderEvent.WORKER_ACCEPT, OrderStatus.ASSIGNED),
            new OrderTransition(OrderStatus.DISPATCHING, OrderEvent.USER_CANCEL, OrderStatus.CANCELLED),
            new OrderTransition(OrderStatus.ASSIGNED, OrderEvent.WORKER_DEPART, OrderStatus.EN_ROUTE),
            new OrderTransition(OrderStatus.ASSIGNED, OrderEvent.WORKER_REJECT, OrderStatus.DISPATCHING),
            new OrderTransition(OrderStatus.ASSIGNED, OrderEvent.RESCHEDULE, OrderStatus.ASSIGNED),
            new OrderTransition(OrderStatus.ASSIGNED, OrderEvent.USER_CANCEL, OrderStatus.CANCELLED),
            new OrderTransition(OrderStatus.EN_ROUTE, OrderEvent.CHECK_IN, OrderStatus.SERVING),
            new OrderTransition(OrderStatus.EN_ROUTE, OrderEvent.USER_CANCEL, OrderStatus.CANCELLED),
            new OrderTransition(OrderStatus.SERVING, OrderEvent.SUBMIT_COMPLETE, OrderStatus.PENDING_CONFIRM),
            new OrderTransition(OrderStatus.PENDING_CONFIRM, OrderEvent.USER_CONFIRM, OrderStatus.COMPLETED),
            new OrderTransition(OrderStatus.PENDING_CONFIRM, OrderEvent.AUTO_CONFIRM, OrderStatus.COMPLETED),
            new OrderTransition(OrderStatus.PENDING_CONFIRM, OrderEvent.USER_COMPLAINT, OrderStatus.AFTER_SALE),
            new OrderTransition(OrderStatus.AFTER_SALE, OrderEvent.ARBITRATION_COMPLETE, OrderStatus.COMPLETED),
            new OrderTransition(OrderStatus.AFTER_SALE, OrderEvent.ARBITRATION_REFUND, OrderStatus.CANCELLED)
    );

    private static final Map<OrderStatus, Map<OrderEvent, OrderStatus>> TABLE = buildTable();

    private OrderStateMachine() {
    }

    public static OrderStatus next(OrderStatus current, OrderEvent event) {
        Map<OrderEvent, OrderStatus> events = TABLE.get(current);
        OrderStatus target = events == null ? null : events.get(event);
        if (target == null) {
            throw new BizException(ErrorCode.ORDER_STATE_INVALID,
                    "订单状态 " + current + " 不允许事件 " + event);
        }
        return target;
    }

    public static boolean canTransit(OrderStatus current, OrderEvent event) {
        Map<OrderEvent, OrderStatus> events = TABLE.get(current);
        return events != null && events.containsKey(event);
    }

    private static Map<OrderStatus, Map<OrderEvent, OrderStatus>> buildTable() {
        Map<OrderStatus, Map<OrderEvent, OrderStatus>> table = new EnumMap<>(OrderStatus.class);
        for (OrderTransition transition : TRANSITIONS) {
            table.computeIfAbsent(transition.from(), key -> new EnumMap<>(OrderEvent.class))
                    .put(transition.event(), transition.to());
        }
        Map<OrderStatus, Map<OrderEvent, OrderStatus>> immutable = new EnumMap<>(OrderStatus.class);
        table.forEach((status, events) -> immutable.put(status, Collections.unmodifiableMap(events)));
        return Collections.unmodifiableMap(immutable);
    }
}
