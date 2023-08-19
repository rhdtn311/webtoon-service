package com.kongtoon.domain.user.model;

import com.mysema.commons.lang.Assert;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class LoginId {

    private static final int MIN_ID_LENGTH = 5;
    private static final int MAX_ID_LENGTH = 20;
    private static final String INVALID_ID_LENGTH_MESSAGE = "로그인 ID 길이 검증에 실패했습니다.";

    @NotBlank
    @Length(min = MIN_ID_LENGTH, max = MAX_ID_LENGTH)
    @Column(name = "login_id", unique = true, length = 15, nullable = false)
    private String idValue;

    public LoginId(String idValue) {
        Assert.isTrue(validatedLoginIdLength(idValue), INVALID_ID_LENGTH_MESSAGE);

        this.idValue = idValue;
    }

    private boolean validatedLoginIdLength(String idValue) {
        return idValue.length() >= MIN_ID_LENGTH && idValue.length() <= MAX_ID_LENGTH;
    }
}
