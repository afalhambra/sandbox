package com.redhat.service.smartevents.integration.tests.steps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.awaitility.Awaitility;

import com.redhat.service.smartevents.integration.tests.common.AwaitilityOnTimeOutHandler;
import com.redhat.service.smartevents.integration.tests.context.TestContext;
import com.redhat.service.smartevents.integration.tests.context.resolver.ContextResolver;
import com.redhat.service.smartevents.integration.tests.resources.PerformanceResource;
import com.redhat.service.smartevents.integration.tests.resources.webhook.performance.WebhookPerformanceResource;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceSteps {

    private static final String ACCESS_TOKEN_REGEX = "\\$\\{perf.access.token\\}";
    private static final String PERF_TEST_NAME_REGEX = "\\$\\{perf.test.name\\}";

    private final TestContext context;

    public PerformanceSteps(TestContext context) {
        this.context = context;
    }

    @When("^Create benchmark \"([^\"]*)\" on Hyperfoil \"([^\"]*)\" instance within (\\d+) (?:minute|minutes) with content:$")
    public void createBenchmarkOnHyperfoilWithinMinutesWithContent(String perfTestName, String hfInstance, int timeoutMinutes, String benchmarkRequestJson) {
        String resolvedBenchmarkRequestJson = ContextResolver.resolveWithScenarioContext(context, benchmarkRequestJson
                                                                                                    .replaceAll(ACCESS_TOKEN_REGEX, context.getManagerToken())
                                                                                                    .replaceAll(PERF_TEST_NAME_REGEX, perfTestName));

        try (InputStream resourceStream = new ByteArrayInputStream(resolvedBenchmarkRequestJson.getBytes(StandardCharsets.UTF_8))) {
            PerformanceResource.addBenchmark(context.getManagerToken(), resourceStream);
            context.getScenario().log("Benchmark created as below\n\"" + resolvedBenchmarkRequestJson + "\n\"");
        } catch (IOException e) {
            throw new UncheckedIOException("Error with inputstream", e);
        }

        Awaitility.await()
                .conditionEvaluationListener(new AwaitilityOnTimeOutHandler(() -> PerformanceResource
                        .getBenchmarkDetailsResponse(context.getManagerToken(), perfTestName).then().log().all()))
                .atMost(Duration.ofMinutes(timeoutMinutes))
                .pollInterval(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> PerformanceResource
                                .getBenchmarkDetailsResponse(context.getManagerToken(), perfTestName)
                                .then()
                                .statusCode(200));
    }

    @Then("^Run benchmark \"([^\"]*)\" on Hyperfoil \"([^\"]*)\" instance within (\\d+) (?:minute|minutes)$")
    public void runBenchmarkOnHyperfoilWithinMinutes(String perfTestName, String hfInstance, int timeoutMinutes) {
        String runId = PerformanceResource.runBenchmark(context.getManagerToken(), perfTestName);
        context.getScenario().log("Running benchmark ID " + runId);

        Awaitility.await()
                .conditionEvaluationListener(new AwaitilityOnTimeOutHandler(() -> PerformanceResource
                        .getRunStatusDetailsResponse(context.getManagerToken(), runId).then().log().all()))
                .atMost(Duration.ofMinutes(timeoutMinutes))
                .pollInterval(Duration.ofSeconds(15))
                .untilAsserted(
                        () -> {
                            String response = PerformanceResource
                                    .getRunStatusDetailsResponse(context.getManagerToken(), runId)
                                    .then()
                                    .statusCode(200)
                                    .extract()
                                    .body()
                                    .asPrettyString();
                            // workaround as /run APIs responses from Hyperfoil don't have any contentType header set at all.
                            JsonObject json = new JsonObject(response);
                            assertThat(json.getBoolean("completed")).isTrue();
                        });
    }

    @And("^number of cloud events sent is greater than (\\d+)$")
    public void numberOfCloudEventsIsGreaterThan(int events) {
        assertThat(WebhookPerformanceResource.getAllEventsCount()).isGreaterThan(events);
    }
}
