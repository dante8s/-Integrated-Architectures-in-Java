package org.example.integrated_architectures;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Uses the Postgres from compose.yaml for now; will be replaced by Testcontainers.
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
class IntegratedArchitecturesApplicationTests {

    @Test
    void contextLoads() {
    }

}
