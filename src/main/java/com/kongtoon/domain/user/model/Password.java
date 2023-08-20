package com.kongtoon.domain.user.model;

import com.kongtoon.common.constant.RegexConst;
import com.kongtoon.common.security.PasswordEncoder;
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
public class Password {

    private static final String INVALID_PASSWORD_MESSAGE = "비밀번호 형식이 일치하지 않습니다.";

    @Pattern(regexp = RegexConst.PASSWORD_VALID_REGEX, message = INVALID_PASSWORD_MESSAGE)
    @Length(min = 8, max = 30)
    @NotBlank
    @Column(name = "password", length = 255, nullable = false)
    private String passwordValue;

    public void encryptPassword(PasswordEncoder passwordEncoder) {
        this.passwordValue = passwordEncoder.encrypt(this.passwordValue);
    }
}
