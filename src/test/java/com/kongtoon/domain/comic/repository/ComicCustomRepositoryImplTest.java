package com.kongtoon.domain.comic.repository;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.domain.view.repository.ViewRepository;
import com.kongtoon.support.dummy.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    @Autowired
    RealtimeComicRankingRepository realtimeComicRankingRepository;

    User user;
    Author author;

    @BeforeEach
    void init() {
        user = userRepository.save(UserDummy.createUser());
        author = authorRepository.save(AuthorDummy.createAuthor(user));
    }

    @Nested
    @Transactional
    @DisplayName("장르별 웹툰 목록 조회 성공")
    class GetComicsByGenreSuccess {

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

        private void saveActionAndDramaGenreComics() {
            Comic actionComic2 = ComicDummy.createComic("actionName2", Genre.ACTION, author);
            Comic actionComic1 = ComicDummy.createComic("actionName1", Genre.ACTION, author);
            Comic actionComic3 = ComicDummy.createComic("actionName3", Genre.ACTION, author);
            Comic dramaComic1 = ComicDummy.createComic("dramaName1", Genre.DRAMA, author);
            Comic dramaComic2 = ComicDummy.createComic("dramaName2", Genre.DRAMA, author);

            comicRepository.saveAll(List.of(actionComic1, actionComic2, actionComic3, dramaComic1, dramaComic2));
        }

        @Test
        @DisplayName("장르별 웹툰 목록 조회에 성공한다. - 최신 에피소드의 조회수가 높은 순서대로 조회")
        void getComicsByGenreSortedByViewCountSuccess() {
            // given
            Comic lastViewedComic = saveActionGenreComic("lastViewComic");
            Comic secondViewedComic = saveActionGenreComic("secondViewComic");
            Comic mostViewedComic = saveActionGenreComic("mostViewComic");

            List<User> viewers = setViewers();
            giveViewsToComicLimitFive(mostViewedComic, 5, viewers);
            giveViewsToComicLimitFive(secondViewedComic, 3, viewers);
            giveViewsToComicLimitFive(lastViewedComic, 1, viewers);

            // when
            List<ComicByGenreResponse> actionGenreComics = getActionGenreComics();

            // then
            assertAll(
                    () -> assertThat(actionGenreComics).first().extracting(ComicByGenreResponse::name).isEqualTo(mostViewedComic.getName()),
                    () -> assertThat(actionGenreComics).element(1).extracting(ComicByGenreResponse::name).isEqualTo(secondViewedComic.getName()),
                    () -> assertThat(actionGenreComics).last().extracting(ComicByGenreResponse::name).isEqualTo(lastViewedComic.getName())
            );
        }

        private Comic saveActionGenreComic(String name) {
            return comicRepository.save(ComicDummy.createComic(name, Genre.ACTION, author));
        }

        private List<User> setViewers() {
            User viewer1 = UserDummy.createUser(new Email("email1@email.com"), new LoginId("loginId1"));
            User viewer2 = UserDummy.createUser(new Email("email2@email.com"), new LoginId("loginId2"));
            User viewer3 = UserDummy.createUser(new Email("email3@email.com"), new LoginId("loginId3"));
            User viewer4 = UserDummy.createUser(new Email("email4@email.com"), new LoginId("loginId4"));
            User viewer5 = UserDummy.createUser(new Email("email5@email.com"), new LoginId("loginId5"));
            List<User> viewers = List.of(viewer1, viewer2, viewer3, viewer4, viewer5);
            userRepository.saveAll(viewers);

            return viewers;
        }

        private void giveViewsToComicLimitFive(Comic comic, int viewCount, List<User> viewers) {
            if (viewCount > 5) {
                throw new IllegalArgumentException("조회수는 5이하로 넣어주세요.");
            }
            Episode episode = EpisodeDummy.createEpisode(comic);
            episodeRepository.save(episode);

            for (int i = 0; i < viewCount; i++) {
                viewRepository.save(ViewDummy.createView(viewers.get(i), episode));
            }
        }

        private List<ComicByGenreResponse> getActionGenreComics() {
            return comicRepository.findComicsByGenre(Genre.ACTION);
        }
    }

    @Nested
    @Transactional
    @DisplayName("실시간 인기 웹툰 목록 조회 성공")
    class GetRealtimeRankingComics {

        @Test
        @DisplayName("실시간 인기 웹툰 목록 조회에 성공한다.")
        void getComicsByComicsByRealtimeRankingSuccess() {
            // given
            RealtimeComicRanking rank1ComicName = saveRealtimeComicRanking("rank1ComicName", 1);
            RealtimeComicRanking rank2ComicName = saveRealtimeComicRanking("rank2ComicName", 2);
            RealtimeComicRanking rank3ComicName = saveRealtimeComicRanking("rank3ComicName", 3);
            RealtimeComicRanking rank4ComicName = saveRealtimeComicRanking("rank4ComicName", 4);
            RealtimeComicRanking rank5ComicName = saveRealtimeComicRanking("rank5ComicName", 5);
            RealtimeComicRanking rank6ComicName = saveRealtimeComicRanking("rank6ComicName", 6);
            RealtimeComicRanking rank7ComicName = saveRealtimeComicRanking("rank7ComicName", 7);
            RealtimeComicRanking rank8ComicName = saveRealtimeComicRanking("rank8ComicName", 8);
            RealtimeComicRanking rank9ComicName = saveRealtimeComicRanking("rank9ComicName", 9);
            RealtimeComicRanking rank10ComicName = saveRealtimeComicRanking("rank10ComicName", 10);

            LocalDate recordDate = rank10ComicName.getRecordDate();

            // when
            List<ComicByRealtimeRankingResponse> comicsByRealtimeRankingResult = comicRepository.findComicsByRealtimeRanking(recordDate, TwoHourSlice.HOUR_02_04);

            // then
            assertAll(
                    () -> assertThat(rank1ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(0).name()),
                    () -> assertThat(rank2ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(1).name()),
                    () -> assertThat(rank3ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(2).name()),
                    () -> assertThat(rank4ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(3).name()),
                    () -> assertThat(rank5ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(4).name()),
                    () -> assertThat(rank6ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(5).name()),
                    () -> assertThat(rank7ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(6).name()),
                    () -> assertThat(rank8ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(7).name()),
                    () -> assertThat(rank9ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(8).name()),
                    () -> assertThat(rank10ComicName).extracting(realtimeComicRanking -> realtimeComicRanking.getComic().getName()).isEqualTo(comicsByRealtimeRankingResult.get(9).name())
            );
        }

        private RealtimeComicRanking saveRealtimeComicRanking(String comicName, int rank) {
            Comic comic = ComicDummy.createComic(comicName, author);
            comicRepository.save(comic);

            return realtimeComicRankingRepository.save(
                    RealtimeComicRankingDummy.createRealtimeComicRanking(TwoHourSlice.HOUR_02_04, rank, comic)
            );
        }
    }
}