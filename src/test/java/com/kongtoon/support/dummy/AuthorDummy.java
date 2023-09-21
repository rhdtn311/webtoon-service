package com.kongtoon.support.dummy;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.user.model.User;

public class AuthorDummy {
    public static Author createAuthor(User user) {
        return new Author(
                "author name",
                "introduction",
                "belong",
                user
        );
    }
}
