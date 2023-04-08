package com.kongtoon.common.session;

import javax.servlet.http.HttpSession;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSessionUtil {

	private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

	public static void setLoginMember(HttpSession session, String loginId) {
		session.setAttribute(LOGIN_MEMBER_ID, loginId);
	}
}
