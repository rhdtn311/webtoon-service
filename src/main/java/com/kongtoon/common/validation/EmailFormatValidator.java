package com.kongtoon.common.validation;

import com.kongtoon.common.constant.RegexConst;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class EmailFormatValidator implements ConstraintValidator<EmailValid, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return Pattern.matches(RegexConst.EMAIL_VALID_REGEX, value);
	}
}
