package com.redhat.service.smartevents.manager.core.api;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import com.redhat.service.smartevents.infra.core.exceptions.BridgeErrorService;
import com.redhat.service.smartevents.infra.core.exceptions.mappers.JsonMappingExceptionMapper;

@Provider
@ApplicationScoped
public class ManagerJsonMappingExceptionMapper extends JsonMappingExceptionMapper {

    @Inject
    public ManagerJsonMappingExceptionMapper(BridgeErrorService bridgeErrorService) {
        super(bridgeErrorService);
    }

    @Override
    @PostConstruct
    protected void init() {
        super.init();
    }

}
