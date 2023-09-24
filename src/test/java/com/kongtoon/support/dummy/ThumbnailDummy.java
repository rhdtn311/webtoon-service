package com.kongtoon.support.dummy;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;

public class ThumbnailDummy {
    public static Thumbnail createSmallTypeThumbnail(String imageUrl, Comic comic) {
        return new Thumbnail(
                ThumbnailType.SMALL, imageUrl, comic);
    }

    public static Thumbnail createMainTypeThumbnail(String imageUrl, Comic comic) {
        return new Thumbnail(
                ThumbnailType.MAIN, imageUrl, comic);
    }
}
