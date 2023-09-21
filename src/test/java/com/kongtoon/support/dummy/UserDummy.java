package com.kongtoon.support.dummy;

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
}
