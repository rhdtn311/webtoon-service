package com.kongtoon.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class NotEmptyFileValidator implements ConstraintValidator<NotEmptyFile, MultipartFile> {

	@Override
	public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
		return !value.isEmpty();
	}
}
