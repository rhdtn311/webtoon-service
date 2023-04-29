package com.kongtoon.domain.comic.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ComicRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComicRequestValid {
	String message() default "Invalid request";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
