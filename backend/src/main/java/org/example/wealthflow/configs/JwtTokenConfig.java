package org.example.wealthflow.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtTokenConfig {

    private String secret;
    private long expirationSeconds;
    private long clockSkewSeconds;
}
