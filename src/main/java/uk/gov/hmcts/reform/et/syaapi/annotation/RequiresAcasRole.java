package uk.gov.hmcts.reform.et.syaapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark controller methods that require ACAS-specific roles.
 * Methods annotated with this will require the user to have either
 * 'caseworker-employment-api' or 'et-acas-api' role.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAcasRole {
}
