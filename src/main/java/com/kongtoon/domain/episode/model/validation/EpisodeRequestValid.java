package com.kongtoon.domain.episode.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = EpisodeRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EpisodeRequestValid {
	String message() default "Invalid request";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
