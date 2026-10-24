package com.homeservice.order.statemachine;

import com.homeservice.order.domain.OrderEvent;
import com.homeservice.order.domain.OrderStatus;

public record OrderTransition(OrderStatus from, OrderEvent event, OrderStatus to) {
}
