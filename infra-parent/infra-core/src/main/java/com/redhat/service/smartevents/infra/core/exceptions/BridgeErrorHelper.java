package com.redhat.service.smartevents.infra.core.exceptions;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.service.smartevents.infra.core.exceptions.definitions.platform.InternalPlatformException;

import io.quarkus.runtime.Quarkus;

@ApplicationScoped
public class BridgeErrorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeErrorHelper.class);

    private static final String USER_MESSAGE = "[%s] %s. Please contact support quoting UUID: %s";

    private static final String USER_MESSAGE_UNKNOWN = "An error has occurred. Please contact support quoting UUID: %s";

    @Inject
    BridgeErrorService bridgeErrorService;

    BridgeError deploymentFailedException;

    @PostConstruct
    protected void setup() {
        Optional<BridgeError> error = bridgeErrorService.getError(InternalPlatformException.class);
        if (error.isPresent()) {
            deploymentFailedException = error.get();
        } else {
            LOGGER.error("{} error is not defined in the ErrorsService.", InternalPlatformException.class.getSimpleName());
            Quarkus.asyncExit(1);
        }
    }

    public BridgeErrorInstance getBridgeErrorInstance(Exception e) {
        LOGGER.debug("Mapping '{}' to BridgeError", e.getClass().getName(), e);
        return bridgeErrorService.getError(e.getClass())
                .map(b -> {
                    BridgeErrorInstance bei = new BridgeErrorInstance(b);
                    LOGGER.info("Mapped '{}' to '{}'", e.getClass().getName(), bei);
                    return bei;
                })
                .orElseGet(() -> {
                    LOGGER.debug("'{}' not found in error catalog. Falling back to generic ProvisioningFailureException.", e.getClass().getName());
                    BridgeErrorInstance bei = new BridgeErrorInstance(deploymentFailedException);
                    LOGGER.info("Mapped '{}' to '{}'", e.getClass().getName(), bei);
                    return bei;
                });
    }

    public BridgeErrorInstance getBridgeErrorInstance(int errorId, String errorUUID) {
        return bridgeErrorService.getError(errorId)
                .map(b -> new BridgeErrorInstance(b, errorUUID))
                .orElseGet(() -> {
                    LOGGER.debug("Error Id '{}' not found in error catalog. Falling back to generic ProvisioningFailureException.", errorId);
                    return new BridgeErrorInstance(deploymentFailedException);
                });
    }

    public String makeUserMessage(HasErrorInformation hasErrorInformation) {
        Integer errorId = hasErrorInformation.getErrorId();
        String errorUUID = hasErrorInformation.getErrorUUID();
        if (Objects.isNull(errorId)) {
            return null;
        }
        StringBuilder message = new StringBuilder();
        bridgeErrorService
                .getError(errorId)
                .ifPresentOrElse(bei -> {
                    String reason = bei.getReason();
                    if (reason.endsWith(".")) {
                        reason = reason.substring(0, reason.length() - 1);
                    }
                    message.append(String.format(USER_MESSAGE, bei.getCode(), reason, errorUUID));
                },
                        () -> message.append(String.format(USER_MESSAGE_UNKNOWN, errorUUID)));
        return message.toString();
    }

}
