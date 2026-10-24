package com.homeservice.dispatch.service;

import com.homeservice.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotBookingServiceTest {

    private final SlotBookingService service = new SlotBookingService(null);

    @Test
    void twoHoursRequiresFourSlots() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 9, 0);
        assertThat(service.requiredSlots(start, start.plusHours(2))).isEqualTo(4);
    }

    @Test
    void thirtyMinutesRequiresOneSlot() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 9, 0);
        assertThat(service.requiredSlots(start, start.plusMinutes(30))).isEqualTo(1);
    }

    @Test
    void durationNotMultipleOfSlotIsRejected() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 9, 0);
        assertThatThrownBy(() -> service.requiredSlots(start, start.plusMinutes(45)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("30");
    }

    @Test
    void zeroDurationIsRejected() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 23, 9, 0);
        assertThatThrownBy(() -> service.requiredSlots(start, start))
                .isInstanceOf(BizException.class);
    }
}
