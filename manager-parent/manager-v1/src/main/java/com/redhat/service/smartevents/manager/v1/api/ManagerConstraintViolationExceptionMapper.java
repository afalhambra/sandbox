package com.redhat.service.smartevents.manager.v1.api;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.ws.rs.ext.Provider;

import com.redhat.service.smartevents.infra.core.exceptions.BridgeErrorService;
import com.redhat.service.smartevents.infra.core.exceptions.mappers.ConstraintViolationExceptionMapper;
import com.redhat.service.smartevents.infra.core.models.responses.ErrorResponse;
import com.redhat.service.smartevents.infra.v1.api.V1APIConstants;

@Provider
@ApplicationScoped
public class ManagerConstraintViolationExceptionMapper extends ConstraintViolationExceptionMapper {

    @Inject
    public ManagerConstraintViolationExceptionMapper(BridgeErrorService bridgeErrorService) {
        super(bridgeErrorService);
    }

    @Override
    @PostConstruct
    protected void init() {
        super.init();
    }

    @Override
    protected ErrorResponse toErrorResponse(ConstraintViolation<?> cv) {
        ErrorResponse errorResponse = super.toErrorResponse(cv);
        errorResponse.setHref(V1APIConstants.V1_ERROR_API_BASE_PATH + errorResponse.getId());
        return errorResponse;
    }
}
