package com.gymnetwork;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

class GymMarketplaceApplicationTests extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts up correctly 
        // and Testcontainers PostgreSQL starts successfully
        assertThat(applicationContext).isNotNull();
    }
}
