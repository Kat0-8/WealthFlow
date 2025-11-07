package org.example.wealthflow.user.dtos.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeLoginRequestDto {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newLogin;
}
