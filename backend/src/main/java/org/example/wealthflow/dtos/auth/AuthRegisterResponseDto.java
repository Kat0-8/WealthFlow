package org.example.wealthflow.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.wealthflow.dtos.user.UserResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponseDto {
    private UserResponseDto user;
    private AuthResponseDto token;
}