package com.kongtoon.support.dummy;

import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.*;

public class UserDummy {
    public static User createUser() {
        return new User(
                new LoginId("uniqueLoginId"),
                "Name",
                new Email("uniqueEmail@email.com"),
                "nickname",
                new Password("password123!"),
                UserAuthority.USER,
                true
        );
    }

    public static User createUser(UserAuthority userAuthority) {
        return new User(
                new LoginId("loginId"),
                "Name",
                new Email("email@email.com"),
                "nickname",
                new Password("password123!"),
                userAuthority,
                true
        );
    }

    public static User createUser(Email email, LoginId loginId) {
        return new User(
                loginId,
                "Name",
                email,
                "nickname",
                new Password("password123!"),
                UserAuthority.USER,
                true
        );
    }

    public static User createUser(LoginId loginId, Password password) {
        return new User(
                loginId,
                "Name",
                new Email("uniqueEmail@email.com"),
                "nickname",
                password,
                UserAuthority.USER,
                true
        );
    }


    public static SignupRequest createSignupRequest() {
        return new SignupRequest(
                new LoginId("loginId"),
                "Name",
                new Email("email@email.com"),
                "nickname",
                new Password("password123!")
        );
    }

    public static SignupRequest createSignupRequest(LoginId loginId, Email email) {
        return new SignupRequest(
                loginId,
                "Name",
                email,
                "nickname",
                new Password("password123!")
        );
    }

    public static LoginRequest createLoginRequest() {
        return new LoginRequest(
                new LoginId("loginId"), new Password("password123!")
        );
    }

    public static UserAuthDTO createUserAuth() {
        return new UserAuthDTO(1L, new LoginId("loginId"), UserAuthority.USER);
    }

    public static UserAuthDTO createUserAuth(LoginId loginId, UserAuthority userAuthority) {
        return new UserAuthDTO(1L, loginId, userAuthority);
    }
}
