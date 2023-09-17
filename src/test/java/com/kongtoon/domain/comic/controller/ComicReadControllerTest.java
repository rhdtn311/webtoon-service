package com.kongtoon.domain.comic.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.*;
import com.kongtoon.domain.comic.repository.ComicRepository;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kongtoon.utils.TestUtil.*;
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
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
        ResultActions resultActions = sendGetComicsByGenreRequest(requestGenre);

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
                                .tag(GET_COMICS_BY_GENRE_TAG) // GET_COMICS_BY_GENRE_TAG
                                .summary(GET_COMICS_BY_GENRE_SUMMARY), // GET_COMICS_BY_GENRE_SUMMARY
                        requestParameters(
                                // GENRE_PARAM, GENRE_PARAM_DESCRIPTION
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

    private Comic saveComic(String name, Genre genre) {
        Comic comic = createComic(name, genre, "summary", PublishDayOfWeek.MON, author);
        comicRepository.save(comic);

        return comic;
    }

    private Thumbnail saveThumbnail(String thumbnailUrl, Comic comic) {
        return thumbnailRepository.save(createThumbnail(ThumbnailType.MAIN, thumbnailUrl, comic));
    }

    private ResultActions sendGetComicsByGenreRequest(String requestGenre) throws Exception {
        return mockMvc.perform(RestDocumentationRequestBuilders.get("/comics/by-genre")
                .param("genre", requestGenre)
                .session(session)
        );
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

    private void setSessionForAuthentication() {
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));
    }

    @BeforeEach
    void init() {
        user = createUser(UserAuthority.AUTHOR);
        author = createAuthor(user);

        User viewer1 = createUser(new Email("email1@email.com"), new LoginId("loginId1"));
        User viewer2 = createUser(new Email("email2@email.com"), new LoginId("loginId2"));
        User viewer3 = createUser(new Email("email3@email.com"), new LoginId("loginId3"));
        User viewer4 = createUser(new Email("email4@email.com"), new LoginId("loginId4"));
        User viewer5 = createUser(new Email("email5@email.com"), new LoginId("loginId5"));
        viewers = List.of(viewer1, viewer2, viewer3, viewer4, viewer5);
        userRepository.saveAll(viewers);

        userRepository.save(user);
        authorRepository.save(author);

        setSessionForAuthentication();
    }
}
