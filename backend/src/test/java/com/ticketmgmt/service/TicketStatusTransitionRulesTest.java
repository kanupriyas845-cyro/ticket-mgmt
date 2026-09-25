package com.ticketmgmt.service;

import com.ticketmgmt.exception.InvalidTicketStatusTransitionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketStatusTransitionRulesTest {

    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "OPEN->IN_PROGRESS",
            "OPEN->CANCELLED",
            "IN_PROGRESS->RESOLVED",
            "IN_PROGRESS->CANCELLED",
            "RESOLVED->CLOSED"
    );

    private static final List<String> ALL_STATUSES = List.of(
            "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED"
    );

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allTransitionPairs")
    void shouldEnforceTransitionRules(String from, String to, boolean valid, String expectedMessage) {
        if (valid) {
            assertThatCode(() -> TicketStatusTransitionRules.validate(from, to))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> TicketStatusTransitionRules.validate(from, to))
                    .isInstanceOf(InvalidTicketStatusTransitionException.class)
                    .hasMessage(expectedMessage);
        }
    }

    static Stream<Arguments> allTransitionPairs() {
        return ALL_STATUSES.stream()
                .flatMap(from -> ALL_STATUSES.stream()
                        .map(to -> {
                            boolean valid = VALID_TRANSITIONS.contains(from + "->" + to);
                            String message = from.equals(to)
                                    ? "Ticket is already in status " + from
                                    : "Cannot transition ticket from " + from + " to " + to;
                            return Arguments.of(from, to, valid, message);
                        }));
    }
}
