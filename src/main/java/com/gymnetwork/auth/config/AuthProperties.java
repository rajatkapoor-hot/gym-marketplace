package com.gymnetwork.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Enables demo-only auth shortcuts such as the deterministic OTP and reset-token warning logs.
     */
    private boolean devModeEnabled = false;
}
