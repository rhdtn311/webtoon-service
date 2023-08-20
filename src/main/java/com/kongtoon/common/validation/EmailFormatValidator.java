package com.kongtoon.common.validation;

import com.kongtoon.common.constant.RegexConst;
import com.kongtoon.domain.user.model.Email;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class EmailFormatValidator implements ConstraintValidator<EmailValid, Email> {

	@Override
	public boolean isValid(Email email, ConstraintValidatorContext context) {
		return Pattern.matches(RegexConst.EMAIL_VALID_REGEX, email.getAddress());
	}
}
