package com.kongtoon.domain.user.service;

import org.springframework.stereotype.Service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void login(LoginRequest loginRequest) {
		User user = userRepository.findByLoginId(loginRequest.loginId())
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		validatePasswordIsCorrect(loginRequest.password(), user.getPassword());
	}

	private void validatePasswordIsCorrect(String inputPassword, String originPassword) {
		if (!passwordEncoder.isMatch(inputPassword, originPassword)) {
			throw new BusinessException(ErrorCode.LOGIN_FAIL);
		}
	}
}