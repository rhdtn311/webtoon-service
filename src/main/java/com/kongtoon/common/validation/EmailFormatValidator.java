package com.kongtoon.common.validation;

import com.kongtoon.common.constant.RegexConst;
import com.kongtoon.domain.user.model.Email;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class EmailFormatValidator implements ConstraintValidator<EmailValid, Email> {

	private static final String INVALID_EMAIL_ADDRESS_MESSAGE = "잘못된 형식의 이메일입니다.";
	private static final String ADDRESS_FIELD = "address";

	@Override
	public boolean isValid(Email email, ConstraintValidatorContext context) {
		if (validateEmailAddress(email)) {
			addConstraintViolation(context, INVALID_EMAIL_ADDRESS_MESSAGE, ADDRESS_FIELD);
			return false;
		}

		return true;
	}

	private static boolean validateEmailAddress(Email email) {
		return !Pattern.matches(RegexConst.EMAIL_VALID_REGEX, email.getAddress());
	}

	private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage, String firstNode) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(errorMessage)
				.addPropertyNode(firstNode)
				.addConstraintViolation();
	}
}
