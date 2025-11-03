package org.example.wealthflow.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
}
