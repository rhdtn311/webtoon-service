package com.kongtoon.domain.user.model;

import com.kongtoon.common.constant.RegexConst;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Email {

    private static final String INVALID_EMAIL_MESSAGE = "잘못된 형식의 이메일입니다.";

    @Pattern(regexp = RegexConst.EMAIL_VALID_REGEX, message = INVALID_EMAIL_MESSAGE)
    @Length(max = 320)
    @NotBlank
    @Column(name = "email", unique = true, length = 320, nullable = false)
    private String address;
}
