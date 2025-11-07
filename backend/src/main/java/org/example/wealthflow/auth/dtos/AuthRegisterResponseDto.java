package org.example.wealthflow.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.wealthflow.user.dtos.user.UserResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponseDto {
    private UserResponseDto user;
    private AuthResponseDto token;
}