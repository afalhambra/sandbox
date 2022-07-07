Feature: Sending cloud events performance tests

  Background:
    Given authenticate against Manager
    And create a new Bridge "my-perf-bridge"
    And the Bridge "my-perf-bridge" is existing with status "ready" within 4 minutes
    And the Ingress of Bridge "my-perf-bridge" is available within 2 minutes
    And add a Processor to the Bridge "my-perf-bridge" with body:
    """
    {
        "name": "my-perf-processor",
        "action": {
            "type": "webhook_sink_0.1",
            "parameters": {
                "endpoint": "${env.slack.webhook.performance.url}"
            }
        },
        "transformationTemplate" : "{\"bridgeId\": \"{data.bridgeId}\", \"message\": \"{data.message}\"}"
    }
    """
    And the Processor "my-perf-processor" of the Bridge "my-perf-bridge" is existing with status "ready" within 3 minutes
#    And Deploy quarkus app service "webhook-perf-test" from runtime registry with configuration:
#      | runtime-env | JAVA_OPTIONS | -Xmx10G             |
#      | config      | infra        | external-infinispan |
#    And Infra Runtime "webhook-perf-test" has 1 pods running within 10 minutes
#    And Service "webhook-perf-test" with process name "webhook-perf-test" is available within 3 minutes
#    And Hyperfoil Node scraper is deployed
#    And Hyperfoil Operator is deployed
#    And Hyperfoil instance "hf-controller" is deployed within 5 minutes

  @performance
  Scenario: Send Cloud Event
    When Create benchmark on Hyperfoil "hf-controller" instance with content:
      """yaml
      name: rhose-send-cloud-events
      agents:
      - driver01
      - driver02
      http:
      - host: ${bridge.my-perf-bridge.endpoint.base}
        sharedConnections: 5
        connectionStrategy: ALWAYS_NEW
      phases:
      - send-cloud-events:
          always:
            duration: 5m
            maxDuration: 6m
            users: 1
            scenario:
            - send-cloud-event:
              - httpRequest:
                  POST: ${bridge.my-perf-bridge.endpoint.path}
                  body: |
                    {
                      "specversion": "1.0",
                      "type": "webhook.site.invoked",
                      "source": "WebhookActionTestService",
                      "id": "webhook-test",
                      "data": {
                          "bridgeId": "${bridge.my-perf-bridge.id}",
                          "message": "hello bridge"
                        }
                    }
                  headers:
                    content-type: application/cloudevents+json
                    authorization: Bearer ${manager.authentication.token}
      """
    Then Run benchmark "rhose-send-cloud-events" on Hyperfoil "hf-controller" instance within 15 minutes
    And number of cloud events sent is greater than 0