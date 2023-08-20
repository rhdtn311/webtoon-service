package com.kongtoon.domain.user.model;

import com.kongtoon.common.validation.EmailValid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@ToString
@EmailValid
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Email {

    @Length(max = 320)
    @NotBlank
    @Column(name = "email", unique = true, length = 320, nullable = false)
    private String address;

    public Email(String address) {
        this.address = address;
    }
}
