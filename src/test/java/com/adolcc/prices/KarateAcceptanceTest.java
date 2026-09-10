package com.adolcc.prices;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KarateAcceptanceTest {

    @LocalServerPort
    private int port;

    @Test
    void api_contract_is_satisfied() {
        System.setProperty("karate.baseUrl", "http://localhost:" + port);

        Results results = Runner.path("classpath:features")
                .outputCucumberJson(true)
                .parallel(1);

        assertThat(results.getFailCount())
                .as(results.getErrorMessages())
                .isZero();
    }
}
