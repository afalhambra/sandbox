package com.redhat.service.smartevents.integration.tests.context.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redhat.service.smartevents.integration.tests.context.TestContext;

public class BridgeResolver implements Resolver {

    private static final Pattern BRIDGE_ID_REGEX = Pattern.compile("\\$\\{bridge\\.([^\\.]+)\\.id\\}");
    private static final Pattern URL_REGEX = Pattern.compile("^([a-z][a-z0-9+\\-.]*:(//[^/?#]+)?)?([a-z0-9\\-._~%!$&'()*+,;=:@/]*)");
    private static final Pattern BRIDGE_BASE_URL_REGEX = Pattern.compile("\\$\\{bridge\\.([^\\.]+)\\.endpoint.base\\}");
    private static final Pattern BRIDGE_ENDPOINT_PATH_REGEX = Pattern.compile("\\$\\{bridge\\.([^\\.]+)\\.endpoint.path\\}");

    public boolean match(String placeholder) {
        return BRIDGE_ID_REGEX.matcher(placeholder).find();
    }

    public String replace(String content, TestContext context) {
        String bridgeName = null;
        Matcher matcher = BRIDGE_ID_REGEX.matcher(content);
        while (matcher.find()) {
            bridgeName = matcher.group(1);
        }
        String bridgeEndpoint = context.getBridge(bridgeName).getEndPoint();
        final String finalBridgeName = bridgeName;
        String replacedContent = matcher.replaceAll(match -> context.getBridge(finalBridgeName).getId());

        String baseUrl = null;
        String bridgePath = null;
        matcher = URL_REGEX.matcher(bridgeEndpoint);
        while (matcher.find()) {
            baseUrl = matcher.group(1);
            bridgePath = matcher.group(3);
        }

        matcher = BRIDGE_BASE_URL_REGEX.matcher(replacedContent);
        replacedContent = matcher.replaceAll(baseUrl);

        matcher = BRIDGE_ENDPOINT_PATH_REGEX.matcher(replacedContent);
        replacedContent = matcher.replaceAll(bridgePath);

        return replacedContent;
    }
}
