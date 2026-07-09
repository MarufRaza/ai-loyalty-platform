package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private LocalDateTime issuedAt;
}
