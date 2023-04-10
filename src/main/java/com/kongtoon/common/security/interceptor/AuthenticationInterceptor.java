package com.kongtoon.common.security.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HandlerMethod handlerMethod = (HandlerMethod)handler;

		LoginCheck loginCheck = handlerMethod.getMethodAnnotation(LoginCheck.class);

		if (loginCheck == null) {
			return true;
		}

		HttpSession session = request.getSession();
		UserAuthDTO userAuth = UserSessionUtil.getLoginUserAuth(session);

		if (userAuth == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		validateUserAuthority(userAuth.userAuthority(), loginCheck.authority());

		return true;
	}

	private void validateUserAuthority(UserAuthority userAuthority, UserAuthority permissionAuthority) {
		if (userAuthority == UserAuthority.ADMIN) {
			return;
		}

		if (permissionAuthority == UserAuthority.AUTHOR && userAuthority != UserAuthority.AUTHOR) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
	}
}
