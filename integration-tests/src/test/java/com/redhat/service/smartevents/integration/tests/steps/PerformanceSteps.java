package com.redhat.service.smartevents.integration.tests.steps;

import java.time.Duration;

import org.awaitility.Awaitility;

import com.redhat.service.smartevents.integration.tests.common.AwaitilityOnTimeOutHandler;
import com.redhat.service.smartevents.integration.tests.context.TestContext;
import com.redhat.service.smartevents.integration.tests.context.resolver.ContextResolver;
import com.redhat.service.smartevents.integration.tests.resources.PerformanceResource;
import com.redhat.service.smartevents.integration.tests.resources.webhook.performance.WebhookPerformanceResource;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceSteps {

    private final TestContext context;

    public PerformanceSteps(TestContext context) {
        this.context = context;
    }

    @When("^Create benchmark on Hyperfoil \"([^\"]*)\" instance with content:$")
    public void createBenchmarkOnHyperfoilWithContent(String hfInstance, DocString benchmarkRequest) {
        String resolvedBenchmarkRequest = ContextResolver.resolveWithScenarioContext(context, benchmarkRequest.getContent());

        PerformanceResource.addBenchmark(context.getManagerToken(), resolvedBenchmarkRequest, benchmarkRequest.getContentType());
        context.getScenario().log("Benchmark created as below\n\"" + resolvedBenchmarkRequest + "\n\"");
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
