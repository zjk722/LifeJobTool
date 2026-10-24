package com.homeservice.order.statemachine;

import com.homeservice.common.exception.BizException;
import com.homeservice.order.domain.OrderEvent;
import com.homeservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {

    @Test
    void paySuccessMovesToPaid() {
        assertThat(OrderStateMachine.next(OrderStatus.CREATED, OrderEvent.PAY_SUCCESS))
                .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void cancelWhenServingIsRejected() {
        assertThatThrownBy(() -> OrderStateMachine.next(OrderStatus.SERVING, OrderEvent.USER_CANCEL))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("SERVING");
    }

    @Test
    void rescheduleKeepsAssigned() {
        assertThat(OrderStateMachine.next(OrderStatus.ASSIGNED, OrderEvent.RESCHEDULE))
                .isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void arbitrationRefundMovesToCancelled() {
        assertThat(OrderStateMachine.next(OrderStatus.AFTER_SALE, OrderEvent.ARBITRATION_REFUND))
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void completedIsTerminal() {
        assertThat(OrderStateMachine.canTransit(OrderStatus.COMPLETED, OrderEvent.USER_CANCEL))
                .isFalse();
    }
}
