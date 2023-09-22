package com.kongtoon.domain.comic.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.FileType;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.support.RequestUtil;
import com.kongtoon.support.dummy.AuthorDummy;
import com.kongtoon.support.dummy.ComicDummy;
import com.kongtoon.support.dummy.FileDummy;
import com.kongtoon.support.dummy.UserDummy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kongtoon.utils.TestConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class ComicModifyControllerTest {

    private static final String CREATE_COMIC_TAG = "웹툰 생성";
    private static final String CREATE_COMIC_SUMMARY = "웹툰 생성 성공, 실패 APIs";
    private static final String COMIC_REQUEST_SCHEMA = "ComicRequest";
    private static final String COMIC_REQUEST_COMIC_NAME_FIELD = "comicName";
    private static final String COMIC_REQUEST_GENRE_FIELD = "genre";
    private static final String COMIC_REQUEST_SUMMARY_FIELD = "summary";
    private static final String COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD = "publishDayOfWeek";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD = "thumbnailRequests[0].thumbnailType";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD = "thumbnailRequests[0].thumbnailImage";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD = "thumbnailRequests[1].thumbnailType";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD = "thumbnailRequests[1].thumbnailImage";
    private static final String COMIC_REQUEST_COMIC_NAME_DESCRIPTION = "웹툰 이름";
    private static final String COMIC_REQUEST_GENRE_DESCRIPTION = "장르";
    private static final String COMIC_REQUEST_SUMMARY_DESCRIPTION = "줄거리";
    private static final String COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION = "연재 요일";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION = "썸네일 타입";
    private static final String THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION = "썸네일 이미지 파일";

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    ComicRepository comicRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockBean
    FileStorage fileStorage;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Transactional
    @Test
    @DisplayName("웬툰 생성에 성공한다.")
    void createComicSuccess() throws Exception {
        // given
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        Author author = AuthorDummy.createAuthor(user);
        userRepository.save(user);
        authorRepository.save(author);

        ComicRequest comicRequest = ComicDummy.createComicRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.png", "image/png");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.png", "image/png");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        String url = "imageUrl";

        when(fileStorage.upload(any(), any()))
                .thenReturn(url);
        when(fileStorage.upload(any(), any()))
                .thenReturn(url);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        List<Comic> comics = comicRepository.findAll();
        assertThat(comics).hasSize(1);
        Comic comic = comics.get(0);

        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(header().string("Location", "/comics/" + comic.getId()));

        verify(fileStorage, times(2)).upload(any(MultipartFile.class), any(FileType.class));

        // docs
        resultActions.andDo(
                document("웹툰 생성 성공",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseHeaders(
                                headerWithName("Location").description("저장된 웹툰 URL")
                        )
                )
        );
    }

    @Test
    @DisplayName("웬툰 생성 시 요청하는 사용자가 존재하지 않아 실패한다.")
    void createComicNotExistsUserFail() throws Exception {
        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(0L, new LoginId("notExistLoginId"), UserAuthority.AUTHOR));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.png", "image/png");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.png", "image/png");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.USER_NOT_FOUND.getMessage()));
        resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.USER_NOT_FOUND.name()));

        // docs
        resultActions.andDo(
                document("웹툰 생성 시 사용자가 존재하지 않아 실패한다.",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
                        )
                )
        );
    }

    @Test
    @DisplayName("웬툰 생성 시 요청하는 작가가 존재하지 않아 실패한다.")
    void createComicNotExistsAuthorFail() throws Exception {
        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        userRepository.save(user);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), UserAuthority.AUTHOR));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.png", "image/png");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.png", "image/png");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.AUTHOR_NOT_FOUND.getMessage()));
        resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.AUTHOR_NOT_FOUND.name()));

        // docs
        resultActions.andDo(
                document("웬툰 생성 시 요청하는 작가가 존재하지 않아 실패한다.",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
                        )
                )
        );
    }

    @Test
    @DisplayName("웬툰 생성 시 요청하는 사용자의 권한 문제로 실패한다.")
    void createComicUnauthorizedFail() throws Exception {
        // given
        User user = UserDummy.createUser(UserAuthority.USER);
        userRepository.save(user);

        ComicRequest comicRequest = ComicDummy.createComicRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.png", "image/png");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.png", "image/png");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.UNAUTHORIZED.getMessage()));
        resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.UNAUTHORIZED.name()));

        // docs
        resultActions.andDo(
                document("웬툰 생성 시 요청하는 사용자의 권한 문제로 실패한다.",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
                        )
                )
        );
    }

    @Test
    @DisplayName("웬툰 생성 시 파일 저장소 업로드 문제로 실패한다.")
    void createComicNotUploadedFileFail() throws Exception {
        // given
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        Author author = AuthorDummy.createAuthor(user);
        userRepository.save(user);
        authorRepository.save(author);

        ComicRequest comicRequest = ComicDummy.createComicRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.png", "image/png");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.png", "image/png");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        when(fileStorage.upload(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.FILE_NOT_UPLOAD));

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        resultActions.andExpect(status().isConflict());
        resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.FILE_NOT_UPLOAD.getMessage()));
        resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.FILE_NOT_UPLOAD.name()));

        // docs
        resultActions.andDo(
                document("웬툰 생성 시 파일 저장소 업로드 문제로 실패한다.",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
                        )
                )
        );
    }

    @Test
    @DisplayName("웬툰 생성 시 잘못된 이미지 파일 확장자 문제로 실패한다.")
    void createComicInvalidFileExtensionFail() throws Exception {
        // given
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        Author author = AuthorDummy.createAuthor(user);
        userRepository.save(user);
        authorRepository.save(author);

        ComicRequest comicRequest = ComicDummy.createComicRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));

        MockMultipartFile thumbnailRequestThumbnailImageSmallFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD, "small_thumbnail_image.txt", "text/plain");
        MockMultipartFile thumbnailRequestThumbnailImageMainFieldImage
                = FileDummy.createMockMultipartFile(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD, "main_thumbnail_image.txt", "text/plain");
        MockPart comicRequestComicNameFieldMock = new MockPart(COMIC_REQUEST_COMIC_NAME_FIELD, comicRequest.getComicName().getBytes());
        MockPart comicRequestGenreFieldMock = new MockPart(COMIC_REQUEST_GENRE_FIELD, comicRequest.getGenre().toString().getBytes());
        MockPart comicRequestSummaryFieldMock = new MockPart(COMIC_REQUEST_SUMMARY_FIELD, comicRequest.getSummary().getBytes());
        MockPart comicRequestPublishDayOfWeekMock = new MockPart(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD, comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicRequestThumbnailTypeSmallFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD, comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart thumbnailRequestThumbnailTypeMainFieldMock = new MockPart(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD, comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        when(fileStorage.upload(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.NOT_ALLOWED_EXTENSION));

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(thumbnailRequestThumbnailImageSmallFieldImage)
                .file(thumbnailRequestThumbnailImageMainFieldImage)
                .part(comicRequestComicNameFieldMock)
                .part(comicRequestGenreFieldMock)
                .part(comicRequestSummaryFieldMock)
                .part(comicRequestPublishDayOfWeekMock)
                .part(comicRequestThumbnailTypeSmallFieldMock)
                .part(thumbnailRequestThumbnailTypeMainFieldMock)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicRequestComicNameFieldMock,
                                comicRequestGenreFieldMock,
                                comicRequestSummaryFieldMock,
                                comicRequestPublishDayOfWeekMock,
                                comicRequestThumbnailTypeSmallFieldMock,
                                thumbnailRequestThumbnailTypeMainFieldMock,
                                thumbnailRequestThumbnailImageSmallFieldImage,
                                thumbnailRequestThumbnailImageMainFieldImage
                        )
                ))
                .session(session)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        resultActions.andExpect(status().isConflict());
        resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.NOT_ALLOWED_EXTENSION.getMessage()));
        resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.NOT_ALLOWED_EXTENSION.name()));

        // docs
        resultActions.andDo(
                document("웬툰 생성 시 잘못된 이미지 파일 확장자 문제로 실패한다.",
                        ResourceSnippetParameters.builder()
                                .tag(CREATE_COMIC_TAG)
                                .summary(CREATE_COMIC_SUMMARY)
                                .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                        requestParts(
                                partWithName(COMIC_REQUEST_COMIC_NAME_FIELD).description(COMIC_REQUEST_COMIC_NAME_DESCRIPTION),
                                partWithName(COMIC_REQUEST_GENRE_FIELD).description(COMIC_REQUEST_GENRE_DESCRIPTION),
                                partWithName(COMIC_REQUEST_SUMMARY_FIELD).description(COMIC_REQUEST_SUMMARY_DESCRIPTION),
                                partWithName(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_FIELD).description(COMIC_REQUEST_PUBLISH_DAY_OF_WEEK_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_TYPE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_SMALL_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION),
                                partWithName(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_MAIN_FIELD).description(THUMBNAIL_REQUEST_THUMBNAIL_IMAGE_DESCRIPTION)
                        ),
                        responseFields(
                                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
                        )
                )
        );
    }
}