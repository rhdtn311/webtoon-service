package com.kongtoon.common.session;

import javax.servlet.http.HttpSession;

import com.kongtoon.domain.user.dto.UserAuthDTO;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSessionUtil {

	public static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

	public static void setLoginUserAuth(HttpSession session, UserAuthDTO userAuth) {
		session.setAttribute(LOGIN_MEMBER_ID, userAuth);
	}

	public static UserAuthDTO getLoginUserAuth(HttpSession session) {
		return (UserAuthDTO)session.getAttribute(LOGIN_MEMBER_ID);
	}

	public static void deleteLoginUserAuth(HttpSession session) {
		session.invalidate();
	}
}
