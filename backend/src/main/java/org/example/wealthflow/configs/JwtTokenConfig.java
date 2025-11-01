package org.example.wealthflow.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class JwtTokenConfig {

    private String secret;
    private long accessTokenExpirationSes = 3600;
    private long clockSkewSec = 60;
}
