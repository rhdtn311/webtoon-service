package com.kongtoon.common.security;

public interface PasswordEncoder {

	String encrypt(String password);

	boolean isMatch(String inputPassword, String savedPassword);
}