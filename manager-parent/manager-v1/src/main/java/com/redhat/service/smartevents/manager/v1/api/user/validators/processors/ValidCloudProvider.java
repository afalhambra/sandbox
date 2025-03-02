package com.redhat.service.smartevents.manager.v1.api.user.validators.processors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CloudProviderConstraintValidator.class)
public @interface ValidCloudProvider {

    String message() default "The supplied Cloud Provider details are not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
