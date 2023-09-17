package com.kongtoon.domain.comic.repository;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.PublishDayOfWeek;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.domain.view.repository.ViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kongtoon.utils.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Transactional
@SpringBootTest
class ComicCustomRepositoryImplTest {

    @Autowired
    ComicRepository comicRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    EpisodeRepository episodeRepository;

    @Autowired
    ViewRepository viewRepository;

    User user;
    Author author;
    List<User> viewers;

    @Test
    @DisplayName("장르별 웹툰 목록 조회에 성공한다. - 장르로 조회")
    void getComicsByGenreSuccess() {
        // given
        saveActionAndDramaGenreComics();

        // when
        List<ComicByGenreResponse> actionGenreComics = getActionGenreComics();

        // then
        assertThat(actionGenreComics).allMatch(comic -> comic.name().startsWith("action"));
    }

    @Test
    @DisplayName("장르별 웹툰 목록 조회에 성공한다. - 최신 에피소드의 조회수가 높은 순서대로 조회")
    void getComicsByGenreSortedByViewCountSuccess() {
        // given
        Comic lastViewedComic = saveActionGenreComic("lastViewComic");
        Comic secondViewedComic = saveActionGenreComic("secondViewComic");
        Comic mostViewedComic = saveActionGenreComic("mostViewComic");

        giveViewsToComicLimitFive(mostViewedComic, 5);
        giveViewsToComicLimitFive(secondViewedComic, 3);
        giveViewsToComicLimitFive(lastViewedComic, 1);

        // when
        List<ComicByGenreResponse> actionGenreComics = getActionGenreComics();

        // then
        assertThat(actionGenreComics).first().extracting(ComicByGenreResponse::name).isEqualTo(mostViewedComic.getName());
        assertThat(actionGenreComics).element(1).extracting(ComicByGenreResponse::name).isEqualTo(secondViewedComic.getName());
        assertThat(actionGenreComics).last().extracting(ComicByGenreResponse::name).isEqualTo(lastViewedComic.getName());
    }

    private List<ComicByGenreResponse> getActionGenreComics() {
        return comicRepository.findComicsByGenre(Genre.ACTION);
    }

    private Comic saveActionGenreComic(String name) {
        return comicRepository.save(createComic(name, Genre.ACTION, "summary", PublishDayOfWeek.MON, author));
    }

    private void saveActionAndDramaGenreComics() {
        Comic actionComic1 = createComic("actionName1", Genre.ACTION, "actionSummary1", PublishDayOfWeek.MON, author);
        Comic actionComic2 = createComic("actionName2", Genre.ACTION, "actionSummary2", PublishDayOfWeek.MON, author);
        Comic actionComic3 = createComic("actionName3", Genre.ACTION, "actionSummary3", PublishDayOfWeek.TUE, author);
        Comic dramaComic1 = createComic("dramaName1", Genre.DRAMA, "dramaSummary1", PublishDayOfWeek.TUE, author);
        Comic dramaComic2 = createComic("dramaName2", Genre.DRAMA, "dramaSummary2", PublishDayOfWeek.WED, author);

        comicRepository.saveAll(List.of(actionComic1, actionComic2, actionComic3, dramaComic1, dramaComic2));
    }

    private void giveViewsToComicLimitFive(Comic comic, int viewCount) {
        if (viewCount > 5) {
            throw new IllegalArgumentException("조회수는 5이하로 넣어주세요.");
        }
        Episode episode = createEpisode("episode", 1, "thumbnailUrl", comic);
        episodeRepository.save(episode);

        for (int i = 0; i < viewCount; i++) {
            viewRepository.save(createView(viewers.get(i), episode));
        }
    }

    @BeforeEach
    void init() {
        user = createUser(new Email("email1@email.com"), new LoginId("loginId1"));
        author = createAuthor(user);

        User viewer1 = createUser(new Email("email2@email.com"), new LoginId("loginId2"));
        User viewer2 = createUser(new Email("email3@email.com"), new LoginId("loginId3"));
        User viewer3 = createUser(new Email("email4@email.com"), new LoginId("loginId4"));
        User viewer4 = createUser(new Email("email5@email.com"), new LoginId("loginId5"));
        User viewer5 = createUser(new Email("email6@email.com"), new LoginId("loginId6"));
        viewers = List.of(viewer1, viewer2, viewer3, viewer4, viewer5);
        userRepository.saveAll(viewers);

        userRepository.save(user);
        authorRepository.save(author);
    }
}