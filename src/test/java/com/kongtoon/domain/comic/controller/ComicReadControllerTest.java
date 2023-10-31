package com.kongtoon.domain.comic.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.RealtimeComicRankingRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.domain.view.repository.ViewRepository;
import com.kongtoon.support.dummy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class ComicReadControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    ComicRepository comicRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EpisodeRepository episodeRepository;

    @Autowired
    ViewRepository viewRepository;

    @Autowired
    ThumbnailRepository thumbnailRepository;

    @Autowired
    RealtimeComicRankingRepository realtimeComicRankingRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @Transactional
    @DisplayName("장르별 웹툰 목록 조회 성공")
    class GetComicsByGenreSuccess {

        @Test
        @DisplayName("장르별 웹툰 목록 조회에 성공한다.")
        void getComicsByGenreSuccess() throws Exception {
            // given
            User user = UserDummy.createUser();
            MockHttpSession loginSession = loginUser(user.getId(), user.getLoginId(), user.getAuthority());

            Author author = saveAuthor();
            Comic mostViewedComic = saveComic("mostViewedComic", Genre.ACTION, author);
            Comic secondViewedComic = saveComic("secondViewedComic", Genre.ACTION, author);
            Comic anotherGenreComic = saveComic("anotherGenreComic", Genre.DRAMA, author);
            Thumbnail mostViewComicThumbnail = saveThumbnail("mostViewedComicThumbnail", mostViewedComic);
            Thumbnail secondViewComicThumbnail = saveThumbnail("secondViewComicThumbnail", secondViewedComic);

            List<User> viewers = setViewers();
            giveViewsToComicLimitFive(mostViewedComic, 5, viewers);
            giveViewsToComicLimitFive(secondViewedComic, 1, viewers);
            giveViewsToComicLimitFive(anotherGenreComic, 3, viewers);

            String requestGenre = Genre.ACTION.name();

            // when
            ResultActions resultActions = requestGetComicsByGenre(loginSession, setParameterForGenre(requestGenre));

            // then
            resultActions.andExpectAll(
                    status().isOk(),
                    jsonPath("[0].id").value(mostViewedComic.getId()),
                    jsonPath("[0].name").value(mostViewedComic.getName()),
                    jsonPath("[0].author").value(mostViewedComic.getAuthor().getAuthorName()),
                    jsonPath("[0].thumbnailUrl").value(mostViewComicThumbnail.getImageUrl()),
                    jsonPath("[0].isNew").value(true),
                    jsonPath("[0].viewCount").value(5),
                    jsonPath("[1].id").value(secondViewedComic.getId()),
                    jsonPath("[1].name").value(secondViewedComic.getName()),
                    jsonPath("[1].author").value(secondViewedComic.getAuthor().getAuthorName()),
                    jsonPath("[1].thumbnailUrl").value(secondViewComicThumbnail.getImageUrl()),
                    jsonPath("[1].isNew").value(true),
                    jsonPath("[1].viewCount").value(1)
            );

            // docs
            resultActions.andDo(
                    document("장르별 웹툰 목록 조회 성공",
                            ResourceSnippetParameters.builder()
                                    .tag("장르별 웹툰 목록 조회")
                                    .summary("장르별 웹툰 목록 조회 성공 APIs"),
                            requestParameters(
                                    parameterWithName("genre").description("조회할 장르")
                            ),
                            responseFields(
                                    fieldWithPath("[].id").description("웹툰 id"),
                                    fieldWithPath("[].name").description("웹툰 이름"),
                                    fieldWithPath("[].author").description("웹툰 작가 이름"),
                                    fieldWithPath("[].thumbnailUrl").description("웹툰 썸네일 URL"),
                                    fieldWithPath("[].isNew").description("신작 웹툰 여부"),
                                    fieldWithPath("[].viewCount").description("최신 에피소드 조회수")
                            )
                    )
            );
        }

        private MultiValueMap<String, String> setParameterForGenre(String requestGenre) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("genre", requestGenre);

            return params;
        }

        private ResultActions requestGetComicsByGenre(MockHttpSession session, MultiValueMap<String, String> params) throws Exception {
            return mockMvc.perform(get("/comics/by-genre")
                    .params(params)
                    .session(session)
            );
        }
    }

    @Nested
    @Transactional
    @DisplayName("실시간 인기 웹툰 목록 조회 성공")
    class GetRealtimeRankingComics {

        @Test
        @DisplayName("실시간 인기 웹툰 목록 조회에 성공한다.")
        void getComicsByRealtimeRankingSuccess() throws Exception {
            // given
            User user = UserDummy.createUser();
            MockHttpSession loginSession = loginUser(user.getId(), user.getLoginId(), user.getAuthority());

            TwoHourSlice prevTwoHourSlice = getPrevTwoHourSlice();

            Author author = saveAuthor();
            List<RealtimeComicRanking> realtimeComicRankingsOrderedByRank = save10RealtimeRankingComicsAtSameTime(prevTwoHourSlice, author);

            // when
            ResultActions resultActions = requestGetComicsByRealtimeRanking(loginSession);

            // then
            for (int i = 0; i < realtimeComicRankingsOrderedByRank.size(); i++) {
                resultActions.andExpectAll(
                        status().isOk(),
                        jsonPath("[" + i + "]" + ".id").value(realtimeComicRankingsOrderedByRank.get(i).getComic().getId()),
                        jsonPath("[" + i + "]" + ".rank").value(realtimeComicRankingsOrderedByRank.get(i).getRank()),
                        jsonPath("[" + i + "]" + ".name").value(realtimeComicRankingsOrderedByRank.get(i).getComic().getName()),
                        jsonPath("[" + i + "]" + ".author").value(realtimeComicRankingsOrderedByRank.get(i).getComic().getAuthor().getAuthorName()),
                        jsonPath("[" + i + "]" + ".thumbnailUrl").value(realtimeComicRankingsOrderedByRank.get(i).getComic().getSmallTypeThumbnailUrl()),
                        jsonPath("[" + i + "]" + ".views").value(realtimeComicRankingsOrderedByRank.get(i).getViews())
                );
            }

            // docs
            resultActions.andDo(
                    document("실시간 인기 웹툰 목록 조회 성공",
                            ResourceSnippetParameters.builder()
                                    .tag("실시간 인기 웹툰 목록 조회")
                                    .summary("실시간 웹툰 목록 조회 성공 APIs"),
                            responseFields(
                                    fieldWithPath("[].id").description("웹툰 id"),
                                    fieldWithPath("[].rank").description("랭킹"),
                                    fieldWithPath("[].name").description("웹툰 이름"),
                                    fieldWithPath("[].author").description("웹툰 작가 이름"),
                                    fieldWithPath("[].thumbnailUrl").description("웹툰 썸네일 URL"),
                                    fieldWithPath("[].views").description("조회수")
                            )
                    )
            );
        }

        private TwoHourSlice getPrevTwoHourSlice() {
            return TwoHourSlice.getPrev(LocalTime.now());
        }

        private List<RealtimeComicRanking> save10RealtimeRankingComicsAtSameTime(TwoHourSlice prevTwoHourSlice, Author author) {
            RealtimeComicRanking rank1ComicName = saveRealtimeRankingComic("rank1ComicName", 1, prevTwoHourSlice, author);
            RealtimeComicRanking rank2ComicName = saveRealtimeRankingComic("rank2ComicName", 2, prevTwoHourSlice, author);
            RealtimeComicRanking rank3ComicName = saveRealtimeRankingComic("rank3ComicName", 3, prevTwoHourSlice, author);
            RealtimeComicRanking rank4ComicName = saveRealtimeRankingComic("rank4ComicName", 4, prevTwoHourSlice, author);
            RealtimeComicRanking rank5ComicName = saveRealtimeRankingComic("rank5ComicName", 5, prevTwoHourSlice, author);
            RealtimeComicRanking rank6ComicName = saveRealtimeRankingComic("rank6ComicName", 6, prevTwoHourSlice, author);
            RealtimeComicRanking rank7ComicName = saveRealtimeRankingComic("rank7ComicName", 7, prevTwoHourSlice, author);
            RealtimeComicRanking rank8ComicName = saveRealtimeRankingComic("rank8ComicName", 8, prevTwoHourSlice, author);
            RealtimeComicRanking rank9ComicName = saveRealtimeRankingComic("rank9ComicName", 9, prevTwoHourSlice, author);
            RealtimeComicRanking rank10ComicName = saveRealtimeRankingComic("rank10ComicName", 10, prevTwoHourSlice, author);

            return List.of(
                    rank1ComicName, rank2ComicName, rank3ComicName, rank4ComicName, rank5ComicName,
                    rank6ComicName, rank7ComicName, rank8ComicName, rank9ComicName, rank10ComicName
            );
        }

        private RealtimeComicRanking saveRealtimeRankingComic(String comicName, int rank, TwoHourSlice twoHourSlice, Author author) {
            Comic comic = ComicDummy.createComic(comicName, author);
            Thumbnail thumbnail = ThumbnailDummy.createSmallTypeThumbnail("thumbnailUrl", comic);
            comicRepository.save(comic);
            thumbnailRepository.save(thumbnail);

            return realtimeComicRankingRepository.save(
                    RealtimeComicRankingDummy.createRealtimeComicRanking(twoHourSlice, rank, comic)
            );
        }

        private ResultActions requestGetComicsByRealtimeRanking(MockHttpSession session) throws Exception {
            return mockMvc.perform(get("/comics/real-time-ranking")
                    .session(session));
        }
    }

    private Comic saveComic(String name, Genre genre, Author author) {
        Comic comic = ComicDummy.createComic(name, genre, author);
        comicRepository.save(comic);

        return comic;
    }

    private Thumbnail saveThumbnail(String thumbnailUrl, Comic comic) {
        return thumbnailRepository.save(ThumbnailDummy.createMainTypeThumbnail(thumbnailUrl, comic));
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

    private MockHttpSession loginUser(Long userId, LoginId loginId, UserAuthority userAuthority) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(userId, loginId, userAuthority));
        return session;
    }

    private Author saveAuthor() {
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        Author author = AuthorDummy.createAuthor(user);
        userRepository.save(user);
        authorRepository.save(author);
        return author;
    }
}
