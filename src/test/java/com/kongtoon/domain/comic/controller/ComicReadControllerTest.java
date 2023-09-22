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
import com.kongtoon.support.RequestUtil;
import com.kongtoon.support.dummy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
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

    private static final String GET_COMICS_BY_GENRE_TAG = "장르별 웹툰 목록 조회";
    private static final String GET_COMICS_BY_GENRE_SUMMARY = "장르별 웹툰 목록 조회 성공 APIs";
    private static final String GENRE_PARAM = "genre";
    private static final String GENRE_PARAM_DESCRIPTION = "조회할 장르";
    private static final String COMIC_BY_GENRE_RESPONSE_ID_FIELD = "[].id";
    private static final String COMIC_BY_GENRE_RESPONSE_ID_FIELD_DESCRIPTION = "웹툰 id";
    private static final String COMIC_BY_GENRE_RESPONSE_NAME_FIELD = "[].name";
    private static final String COMIC_BY_GENRE_RESPONSE_NAME_FIELD_DESCRIPTION = "웹툰 이름";
    private static final String COMIC_BY_GENRE_RESPONSE_AUTHOR_FIELD = "[].author";
    private static final String COMIC_BY_GENRE_RESPONSE_AUTHOR_DESCRIPTION = "웹툰 작가 이름";
    private static final String COMIC_BY_GENRE_RESPONSE_THUMBNAIL_FIELD = "[].thumbnailUrl";
    private static final String COMIC_BY_GENRE_RESPONSE_THUMBNAIL_DESCRIPTION = "웹툰 썸네일 URL";
    private static final String COMIC_BY_GENRE_RESPONSE_IS_NEW_FIELD = "[].isNew";
    private static final String COMIC_BY_GENRE_RESPONSE_IS_NEW_DESCRIPTION = "신작 웹툰 여부";
    private static final String COMIC_BY_GENRE_RESPONSE_VIEW_COUNT_FIELD = "[].viewCount";
    private static final String COMIC_BY_GENRE_RESPONSE_VIEW_COUNT_DESCRIPTION = "최신 에피소드 조회수";

    private static final String GET_COMICS_BY_REALTIME_RANKING_TAG = "실시간 인기 웹툰 목록 조회";
    private static final String GET_COMICS_BY_REALTIME_RANKING_SUMMARY = "실시간 웹툰 목록 조회 성공 APIs";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_ID_FIELD = "[].id";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_ID_DESCRIPTION = "웹툰 id";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_RANK_FIELD = "[].rank";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_RANK_DESCRIPTION = "랭킹";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_NAME_FIELD = "[].name";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_NAME_DESCRIPTION = "웹툰 이름";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_AUTHOR_FIELD = "[].author";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_AUTHOR_DESCRIPTION = "웹툰 작가 이름";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_THUMBNAIL_FIELD = "[].thumbnailUrl";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_THUMBNAIL_DESCRIPTION = "웹툰 썸네일 URL";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_VIEWS_FIELD = "[].views";
    private static final String COMICS_BY_REALTIME_RANKING_RESPONSE_VIEWS_DESCRIPTION = "조회수";

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
    ObjectMapper objectMapper;

    @Autowired
    RequestUtil requestUtil;

    User user;
    Author author;
    List<User> viewers;
    MockHttpSession session = new MockHttpSession();

    @Transactional
    @Test
    @DisplayName("장르별 웹툰 목록 조회에 성공한다.")
    void getComicsByGenreSuccess() throws Exception {
        // given
        Comic mostViewedComic = saveComic("mostViewedComic", Genre.ACTION);
        Comic secondViewedComic = saveComic("secondViewedComic", Genre.ACTION);
        Comic anotherGenreComic = saveComic("anotherGenreComic", Genre.DRAMA);
        Thumbnail mostViewComicThumbnail = saveThumbnail("mostViewedComicThumbnail", mostViewedComic);
        Thumbnail secondViewComicThumbnail = saveThumbnail("secondViewComicThumbnail", secondViewedComic);

        giveViewsToComicLimitFive(mostViewedComic, 5);
        giveViewsToComicLimitFive(secondViewedComic, 1);
        giveViewsToComicLimitFive(anotherGenreComic, 3);

        String requestGenre = Genre.ACTION.name();

        // when
        ResultActions resultActions = requestUtil.requestGetWithLoginAndParams("/comics/by-genre", user, setParameterForGenre(requestGenre));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("[0].id").value(mostViewedComic.getId()));
        resultActions.andExpect(jsonPath("[0].name").value(mostViewedComic.getName()));
        resultActions.andExpect(jsonPath("[0].author").value(mostViewedComic.getAuthor().getAuthorName()));
        resultActions.andExpect(jsonPath("[0].thumbnailUrl").value(mostViewComicThumbnail.getImageUrl()));
        resultActions.andExpect(jsonPath("[0].isNew").value(true));
        resultActions.andExpect(jsonPath("[0].viewCount").value(5));
        resultActions.andExpect(jsonPath("[1].id").value(secondViewedComic.getId()));
        resultActions.andExpect(jsonPath("[1].name").value(secondViewedComic.getName()));
        resultActions.andExpect(jsonPath("[1].author").value(secondViewedComic.getAuthor().getAuthorName()));
        resultActions.andExpect(jsonPath("[1].thumbnailUrl").value(secondViewComicThumbnail.getImageUrl()));
        resultActions.andExpect(jsonPath("[1].isNew").value(true));
        resultActions.andExpect(jsonPath("[1].viewCount").value(1));

        // docs
        resultActions.andDo(
                document("장르별 웹툰 목록 조회 성공",
                        ResourceSnippetParameters.builder()
                                .tag(GET_COMICS_BY_GENRE_TAG)
                                .summary(GET_COMICS_BY_GENRE_SUMMARY),
                        requestParameters(
                                parameterWithName(GENRE_PARAM).description(GENRE_PARAM_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_ID_FIELD).description(COMIC_BY_GENRE_RESPONSE_ID_FIELD_DESCRIPTION),
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_NAME_FIELD).description(COMIC_BY_GENRE_RESPONSE_NAME_FIELD_DESCRIPTION),
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_AUTHOR_FIELD).description(COMIC_BY_GENRE_RESPONSE_AUTHOR_DESCRIPTION),
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_THUMBNAIL_FIELD).description(COMIC_BY_GENRE_RESPONSE_THUMBNAIL_DESCRIPTION),
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_IS_NEW_FIELD).description(COMIC_BY_GENRE_RESPONSE_IS_NEW_DESCRIPTION),
                                fieldWithPath(COMIC_BY_GENRE_RESPONSE_VIEW_COUNT_FIELD).description(COMIC_BY_GENRE_RESPONSE_VIEW_COUNT_DESCRIPTION)
                        )
                )
        );
    }

    private MultiValueMap<String, String> setParameterForGenre(String requestGenre) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("genre", requestGenre);

        return params;
    }

    @Transactional
    @Test
    @DisplayName("실시간 인기 웹툰 목록 조회에 성공한다.")
    void getComicsByRealtimeRankingSuccess() throws Exception {
        // given
        TwoHourSlice prevTwoHourSlice = getPrevTwoHourSlice();
        String thumbnailUrl = "thumbnailUrl";

        RealtimeComicRanking rank1ComicName = saveRealtimeComicRanking("rank1ComicName", 1, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank2ComicName = saveRealtimeComicRanking("rank2ComicName", 2, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank3ComicName = saveRealtimeComicRanking("rank3ComicName", 3, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank4ComicName = saveRealtimeComicRanking("rank4ComicName", 4, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank5ComicName = saveRealtimeComicRanking("rank5ComicName", 5, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank6ComicName = saveRealtimeComicRanking("rank6ComicName", 6, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank7ComicName = saveRealtimeComicRanking("rank7ComicName", 7, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank8ComicName = saveRealtimeComicRanking("rank8ComicName", 8, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank9ComicName = saveRealtimeComicRanking("rank9ComicName", 9, prevTwoHourSlice, thumbnailUrl);
        RealtimeComicRanking rank10ComicName = saveRealtimeComicRanking("rank10ComicName", 10, prevTwoHourSlice, thumbnailUrl);
        List<RealtimeComicRanking> realtimeComicRankings = List.of(
                rank1ComicName, rank2ComicName, rank3ComicName, rank4ComicName, rank5ComicName,
                rank6ComicName, rank7ComicName, rank8ComicName, rank9ComicName, rank10ComicName
        );

        // when
        ResultActions resultActions = requestUtil.requestGetWithLogin("/comics/real-time/ranking", user);

        // then
        for (int i = 0; i < realtimeComicRankings.size(); i++) {
            resultActions.andExpectAll(
                    status().isOk(),
                    jsonPath("[" + i + "]" + ".id").value(realtimeComicRankings.get(i).getComic().getId()),
                    jsonPath("[" + i + "]" + ".rank").value(realtimeComicRankings.get(i).getRank()),
                    jsonPath("[" + i + "]" + ".name").value(realtimeComicRankings.get(i).getComic().getName()),
                    jsonPath("[" + i + "]" + ".author").value(realtimeComicRankings.get(i).getComic().getAuthor().getAuthorName()),
                    jsonPath("[" + i + "]" + ".thumbnailUrl").value(realtimeComicRankings.get(i).getComic().getSmallTypeThumbnailUrl()),
                    jsonPath("[" + i + "]" + ".views").value(realtimeComicRankings.get(i).getViews())
            );
        }

        // docs
        resultActions.andDo(
                document("실시간 인기 웹툰 목록 조회 성공",
                        ResourceSnippetParameters.builder()
                                .tag(GET_COMICS_BY_REALTIME_RANKING_TAG)
                                .summary(GET_COMICS_BY_REALTIME_RANKING_SUMMARY),
                        responseFields(
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_ID_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_ID_DESCRIPTION),
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_RANK_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_RANK_DESCRIPTION),
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_NAME_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_NAME_DESCRIPTION),
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_AUTHOR_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_AUTHOR_DESCRIPTION),
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_THUMBNAIL_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_THUMBNAIL_DESCRIPTION),
                                fieldWithPath(COMICS_BY_REALTIME_RANKING_RESPONSE_VIEWS_FIELD).description(COMICS_BY_REALTIME_RANKING_RESPONSE_VIEWS_DESCRIPTION)
                        )
                )
        );
    }

    private TwoHourSlice getPrevTwoHourSlice() {
        return TwoHourSlice.getPrev(LocalTime.now());
    }

    private Comic saveComic(String name, Genre genre) {
        Comic comic = ComicDummy.createComic(name, genre, author);
        comicRepository.save(comic);

        return comic;
    }

    private Thumbnail saveThumbnail(String thumbnailUrl, Comic comic) {
        return thumbnailRepository.save(ThumbnailDummy.createMainTypeThumbnail(thumbnailUrl, comic));
    }

    private void giveViewsToComicLimitFive(Comic comic, int viewCount) {
        if (viewCount > 5) {
            throw new IllegalArgumentException("조회수는 5이하로 넣어주세요.");
        }
        Episode episode = EpisodeDummy.createEpisode(comic);
        episodeRepository.save(episode);

        for (int i = 0; i < viewCount; i++) {
            viewRepository.save(ViewDummy.createView(viewers.get(i), episode));
        }
    }

    private void setSessionForAuthentication() {
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));
    }

    private RealtimeComicRanking saveRealtimeComicRanking(String comicName, int rank, TwoHourSlice twoHourSlice, String thumbnailUrl) {
        Comic comic = ComicDummy.createComic(comicName, author);
        Thumbnail thumbnail = ThumbnailDummy.createSmallTypeThumbnail(thumbnailUrl, comic);
        comicRepository.save(comic);
        thumbnailRepository.save(thumbnail);

        return realtimeComicRankingRepository.save(
                RealtimeComicRankingDummy.createRealtimeComicRanking(twoHourSlice, rank, comic)
        );
    }

    @BeforeEach
    void init() {
        user = UserDummy.createUser(UserAuthority.AUTHOR);
        author = AuthorDummy.createAuthor(user);

        User viewer1 = UserDummy.createUser(new Email("email1@email.com"), new LoginId("loginId1"));
        User viewer2 = UserDummy.createUser(new Email("email2@email.com"), new LoginId("loginId2"));
        User viewer3 = UserDummy.createUser(new Email("email3@email.com"), new LoginId("loginId3"));
        User viewer4 = UserDummy.createUser(new Email("email4@email.com"), new LoginId("loginId4"));
        User viewer5 = UserDummy.createUser(new Email("email5@email.com"), new LoginId("loginId5"));
        viewers = List.of(viewer1, viewer2, viewer3, viewer4, viewer5);
        userRepository.saveAll(viewers);

        userRepository.save(user);
        authorRepository.save(author);

        setSessionForAuthentication();
    }
}
