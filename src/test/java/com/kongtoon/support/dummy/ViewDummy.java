package com.kongtoon.support.dummy;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;

public class ViewDummy {
    public static View createView(User user, Episode episode) {
        return new View(user, episode);
    }

}
