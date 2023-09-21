package com.kongtoon.support.dummy;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.PublishDayOfWeek;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;

import java.util.List;

public class ComicDummy {

    public static Comic createComic(String comicName, Author author) {
        return new Comic(
                comicName, Genre.ACTION, "summary", PublishDayOfWeek.MON, author
        );
    }

    public static List<ComicByRealtimeRankingResponse> getComicByRealtimeRankingResponses() {
        return List.of(
                new ComicByRealtimeRankingResponse(1L, 1, "name1", "author1", "thumbnailUrl1", 500L),
                new ComicByRealtimeRankingResponse(2L, 2, "name2", "author2", "thumbnailUrl2", 450L),
                new ComicByRealtimeRankingResponse(3L, 3, "name3", "author3", "thumbnailUrl3", 430L),
                new ComicByRealtimeRankingResponse(4L, 4, "name4", "author4", "thumbnailUrl4", 400L),
                new ComicByRealtimeRankingResponse(5L, 5, "name5", "author5", "thumbnailUrl5", 390L),
                new ComicByRealtimeRankingResponse(6L, 6, "name6", "author6", "thumbnailUrl6", 340L),
                new ComicByRealtimeRankingResponse(7L, 7, "name7", "author7", "thumbnailUrl7", 200L),
                new ComicByRealtimeRankingResponse(8L, 8, "name8", "author8", "thumbnailUrl8", 100L),
                new ComicByRealtimeRankingResponse(9L, 9, "name9", "author9", "thumbnailUrl9", 50L),
                new ComicByRealtimeRankingResponse(10L, 10, "name10", "author10", "thumbnailUrl10", 5L)
        );
    }
}
