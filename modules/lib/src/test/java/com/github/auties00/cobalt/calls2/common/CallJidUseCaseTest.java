package com.github.auties00.cobalt.calls2.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CallJidUseCase recovered codes")
class CallJidUseCaseTest {
    @ParameterizedTest
    @DisplayName("each use-case binds its recovered engine code")
    @CsvSource({
            // Recovered from wa_call_identifier_jid_initialize (fn10519) producers in call_jid.cc.
            "USER, 0", "GROUP, 1", "DEVICE, 5", "GROUP_CALL, 6", "NEWSLETTER, 12", "EXTENSION, 13"
    })
    void recoveredValues(CallJidUseCase useCase, int value) {
        assertEquals(value, useCase.value(), () -> useCase + " must bind its recovered use-case code");
    }

    @Test
    @DisplayName("declares exactly the six concrete codes")
    void closedSet() {
        assertEquals(6, CallJidUseCase.values().length);
    }

    @ParameterizedTest
    @EnumSource(CallJidUseCase.class)
    @DisplayName("each use-case round-trips through its value")
    void valueRoundTrip(CallJidUseCase useCase) {
        assertSame(useCase, CallJidUseCase.ofValue(useCase.value()).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 7, 8, 9, 10, 11, -1, 14})
    @DisplayName("an unused gap code resolves to empty")
    void gapCodesAreEmpty(int gap) {
        assertTrue(CallJidUseCase.ofValue(gap).isEmpty(), () -> "gap code " + gap + " must not resolve");
    }
}
