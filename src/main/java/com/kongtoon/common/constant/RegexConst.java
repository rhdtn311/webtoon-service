package com.kongtoon.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexConst {

	public static final String PASSWORD_VALID_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[\\W]).{8,30})";
}