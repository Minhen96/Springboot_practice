package com.example.mhpractice.common.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElementType.METHOD = Can put on individual methods
 * ElementType.TYPE = Can put on entire class
 * Makes annotation available at runtime
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface StandardReponseBody {

}
