package com.kongtoon.common.security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoder implements PasswordEncoder {

	@Override
	public String encrypt(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	@Override
	public boolean isMatch(String inputPassword, String savedPassword) {
		return BCrypt.checkpw(inputPassword, savedPassword);
	}
}