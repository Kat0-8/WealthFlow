package org.example.wealthflow.user.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
}
