package com.kongtoon.domain.user.service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserAuthDTO login(LoginRequest loginRequest) {
		User user = userRepository.findByLoginId(loginRequest.loginId())
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		validatePasswordIsCorrect(loginRequest.password(), user.getPassword());

		return UserAuthDTO.from(user);
	}

	private void validatePasswordIsCorrect(String inputPassword, String originPassword) {
		if (!passwordEncoder.isMatch(inputPassword, originPassword)) {
			throw new BusinessException(ErrorCode.LOGIN_FAIL);
		}
	}

	@Transactional
	public Long signup(SignupRequest signupRequest) {
		validateDuplicateEmail(signupRequest.email());
		validateDuplicateLoginId(signupRequest.loginId());

		String encryptPassword = passwordEncoder.encrypt(signupRequest.password());
		User user = signupRequest.toEntity(encryptPassword);

		userRepository.save(user);

		return user.getId();
	}

	public void validateDuplicateLoginId(LoginId loginId) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
		}
	}

	public void validateDuplicateEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
	}
}