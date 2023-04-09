package com.kongtoon.domain.user.dto;

import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;

public record UserAuthDTO(String loginId, UserAuthority userAuthority) {
	public static UserAuthDTO from(User user) {
		return new UserAuthDTO(user.getLoginId(), user.getAuthority());
	}
}
