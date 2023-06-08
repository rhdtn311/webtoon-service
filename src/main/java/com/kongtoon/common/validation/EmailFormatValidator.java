package com.kongtoon.common.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.kongtoon.common.constant.RegexConst;

@Component
public class EmailFormatValidator implements ConstraintValidator<Email, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return Pattern.matches(RegexConst.EMAIL_VALID_REGEX, value);
	}
}
