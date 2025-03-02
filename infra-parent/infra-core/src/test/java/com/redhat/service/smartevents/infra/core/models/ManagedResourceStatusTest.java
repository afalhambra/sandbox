package com.redhat.service.smartevents.infra.core.models;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ManagedResourceStatusTest {

    @ParameterizedTest
    @EnumSource(ManagedResourceStatus.class)
    void testSerialisation(ManagedResourceStatus status) {
        assertThat(status.getValue()).isEqualTo(status.name().toLowerCase(Locale.ROOT));
    }

    @ParameterizedTest
    @EnumSource(ManagedResourceStatus.class)
    void testFromString(ManagedResourceStatus status) {
        assertThat(ManagedResourceStatus.fromString(status.value)).isEqualTo(status);
    }

    @Test
    void testFromStringWithInvalidValue() {
        assertThatThrownBy(() -> ManagedResourceStatus.fromString("bananna")).isInstanceOf(IllegalArgumentException.class);
    }

}
