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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.restdocs.request.RequestPartsSnippet;
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

    @Nested
    @Transactional
    @DisplayName("웹툰 생성 성공")
    class ComicCreateSuccess {

        @Test
        @DisplayName("웹툰 생성에 성공한다.")
        void createComicSuccess() throws Exception {
            // given
            User author = saveAuthorUser();
            ComicRequest comicRequest = ComicDummy.createComicRequest();
            MockHttpSession loginUserSession = loginUser(author.getId(), author.getLoginId(), author.getAuthority());
            uploadFileSuccess();

            // when
            ResultActions resultActions = requestCreateComic(
                    "small_thumbnail_image.png",
                    "main_thumbnail_image.png",
                    "image/png",
                    comicRequest,
                    loginUserSession
            );

            List<Comic> comics = comicRepository.findAll();
            Comic comic = comics.get(0);

            // then
            assertThat(comics).hasSize(1);

            resultActions.andExpectAll(
                    status().isCreated(),
                    header().string("Location", "/comics/" + comic.getId())
            );

            verify(fileStorage, times(2)).upload(any(MultipartFile.class), any(FileType.class));

            // docs
            resultActions.andDo(
                    document("웹툰 생성 성공",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA)),
                            createComicRequestDocs(),
                            responseHeaders(
                                    headerWithName("Location").description("저장된 웹툰 URL")
                            )
                    )
            );
        }

        private void uploadFileSuccess() {
            String url = "imageUrl";
            when(fileStorage.upload(any(), any()))
                    .thenReturn(url);
            when(fileStorage.upload(any(), any()))
                    .thenReturn(url);
        }
    }

    @Nested
    @Transactional
    @DisplayName("웹툰 생성 실패")
    class ComicCreateFail {

        @Test
        @DisplayName("웹툰 생성 시 요청하는 사용자가 존재하지 않아 실패한다.")
        void createComicNotExistsUserFail() throws Exception {
            // given
            ComicRequest comicRequest = ComicDummy.createComicRequest();
            LoginId notExistLoginId = new LoginId("notExistLoginId");
            MockHttpSession notExistLoginUserSession = loginUser(0L, notExistLoginId, UserAuthority.AUTHOR);

            // when
            ResultActions resultActions = requestCreateComic(
                    "small_thumbnail_image.png",
                    "main_thumbnail_image.png",
                    "image/png",
                    comicRequest,
                    notExistLoginUserSession
            );

            // then
            resultActions.andExpectAll(
                    status().isNotFound(),
                    jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.USER_NOT_FOUND.getMessage()),
                    jsonPath(ERROR_CODE_FIELD).value(ErrorCode.USER_NOT_FOUND.name())
            );

            // docs
            resultActions.andDo(
                    document("웹툰 생성 시 사용자가 존재하지 않아 실패한다.",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                    .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                            createComicRequestDocs(),
                            createErrorResponseDocs()
                    )
            );
        }

        @Test
        @DisplayName("웹툰 생성 시 요청하는 작가가 존재하지 않아 실패한다.")
        void createComicNotExistsAuthorFail() throws Exception {
            // given
            ComicRequest comicRequest = ComicDummy.createComicRequest();
            User user = saveOnlyUser(UserAuthority.AUTHOR);
            MockHttpSession loginUserSession = loginUser(user.getId(), user.getLoginId(), user.getAuthority());

            // when
            ResultActions resultActions = requestCreateComic(
                    "small_thumbnail_image.png",
                    "main_thumbnail_image.png",
                    "image/png",
                    comicRequest,
                    loginUserSession
            );

            // then
            resultActions.andExpectAll(
                    status().isNotFound(),
                    jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.AUTHOR_NOT_FOUND.getMessage()),
                    jsonPath(ERROR_CODE_FIELD).value(ErrorCode.AUTHOR_NOT_FOUND.name())
            );

            // docs
            resultActions.andDo(
                    document("웬툰 생성 시 요청하는 작가가 존재하지 않아 실패한다.",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                    .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                            createComicRequestDocs(),
                            createErrorResponseDocs()
                    )
            );
        }

        @Test
        @DisplayName("웹툰 생성 시 요청하는 사용자의 권한 문제로 실패한다.")
        void createComicUnauthorizedFail() throws Exception {
            // given
            ComicRequest comicRequest = ComicDummy.createComicRequest();
            User user = saveOnlyUser(UserAuthority.USER);
            MockHttpSession loginUserSession = loginUser(user.getId(), user.getLoginId(), user.getAuthority());

            // when
            ResultActions resultActions = requestCreateComic(
                    "small_thumbnail_image.png",
                    "main_thumbnail_image.png",
                    "image/png",
                    comicRequest,
                    loginUserSession
            );

            // then
            resultActions.andExpectAll(
                    status().isForbidden(),
                    jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.UNAUTHORIZED.getMessage()),
                    jsonPath(ERROR_CODE_FIELD).value(ErrorCode.UNAUTHORIZED.name())
            );

            // docs
            resultActions.andDo(
                    document("웹툰 생성 시 요청하는 사용자의 권한 문제로 실패한다.",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                    .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                            createComicRequestDocs(),
                            createErrorResponseDocs()
                    )
            );
        }

        @Test
        @DisplayName("웹툰 생성 시 파일 저장소 업로드 문제로 실패한다.")
        void createComicNotUploadedFileFail() throws Exception {
            // given
            User author = saveAuthorUser();
            ComicRequest comicRequest = ComicDummy.createComicRequest();
            MockHttpSession loginUserSession = loginUser(author.getId(), author.getLoginId(), author.getAuthority());

            failFileUpload(ErrorCode.FILE_NOT_UPLOAD);

            // when
            ResultActions resultActions = requestCreateComic(
                    "small_thumbnail_image.png",
                    "main_thumbnail_image.png",
                    "image/png",
                    comicRequest,
                    loginUserSession
            );

            // then
            resultActions.andExpectAll(
                    status().isConflict(),
                    jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.FILE_NOT_UPLOAD.getMessage()),
                    jsonPath(ERROR_CODE_FIELD).value(ErrorCode.FILE_NOT_UPLOAD.name())
            );

            // docs
            resultActions.andDo(
                    document("웬툰 생성 시 파일 저장소 업로드 문제로 실패한다.",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                    .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                            createComicRequestDocs(),
                            createErrorResponseDocs()
                    )
            );
        }

        @Test
        @DisplayName("웬툰 생성 시 잘못된 이미지 파일 확장자 문제로 실패한다.")
        void createComicInvalidFileExtensionFail() throws Exception {
            // given
            User author = saveAuthorUser();

            ComicRequest comicRequest = ComicDummy.createComicRequest();
            MockHttpSession loginUserSession = loginUser(author.getId(), author.getLoginId(), author.getAuthority());

            failFileUpload(ErrorCode.NOT_ALLOWED_EXTENSION);
            String notAllowedExtensionFile = "not_allowed_extension_file.txt";

            // when
            ResultActions resultActions = requestCreateComic(
                    notAllowedExtensionFile,
                    notAllowedExtensionFile,
                    "text/plain",
                    comicRequest,
                    loginUserSession
            );

            // then
            resultActions.andExpectAll(
                    status().isConflict(),
                    jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.NOT_ALLOWED_EXTENSION.getMessage()),
                    jsonPath(ERROR_CODE_FIELD).value(ErrorCode.NOT_ALLOWED_EXTENSION.name())
            );

            // docs
            resultActions.andDo(
                    document("웬툰 생성 시 잘못된 이미지 파일 확장자 문제로 실패한다.",
                            ResourceSnippetParameters.builder()
                                    .tag(CREATE_COMIC_TAG)
                                    .summary(CREATE_COMIC_SUMMARY)
                                    .requestSchema(Schema.schema(COMIC_REQUEST_SCHEMA))
                                    .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA)),
                            createComicRequestDocs(),
                            createErrorResponseDocs()
                    )
            );
        }

        private void failFileUpload(ErrorCode fileNotUpload) {
            when(fileStorage.upload(any(), any()))
                    .thenThrow(new BusinessException(fileNotUpload));
        }

        @NotNull
        private User saveOnlyUser(UserAuthority userAuthority) {
            User user = UserDummy.createUser(userAuthority);
            userRepository.save(user);
            return user;
        }
    }

    private RequestPartsSnippet createComicRequestDocs() {
        return requestParts(
                partWithName("comicName").description("웹툰 이름"),
                partWithName("genre").description("장르"),
                partWithName("summary").description("줄거리"),
                partWithName("publishDayOfWeek").description("연재 요일"),
                partWithName("thumbnailRequests[0].thumbnailType").description("썸네일 타입"),
                partWithName("thumbnailRequests[1].thumbnailType").description("썸네일 타입"),
                partWithName("thumbnailRequests[0].thumbnailImage").description("썸네일 이미지 파일"),
                partWithName("thumbnailRequests[1].thumbnailImage").description("썸네일 이미지 파일")
        );
    }

    private ResultActions requestCreateComic(
            String smallThumbnailFileName,
            String mainThumbnailFileName,
            String thumbnailFileContentType,
            ComicRequest comicRequest,
            MockHttpSession loginSession
    ) throws Exception {
        MockMultipartFile smallThumbnailFile = FileDummy.createMockMultipartFile("thumbnailRequests[0].thumbnailImage", smallThumbnailFileName, thumbnailFileContentType);
        MockMultipartFile mainThumbnailFile = FileDummy.createMockMultipartFile("thumbnailRequests[1].thumbnailImage", mainThumbnailFileName, thumbnailFileContentType);
        MockPart comicName = new MockPart("comicName", comicRequest.getComicName().getBytes());
        MockPart comicGenre = new MockPart("genre", comicRequest.getGenre().toString().getBytes());
        MockPart comicSummary = new MockPart("summary", comicRequest.getSummary().getBytes());
        MockPart comicDayOfWeek = new MockPart("publishDayOfWeek", comicRequest.getPublishDayOfWeek().toString().getBytes());
        MockPart comicSmallThumbnailType = new MockPart("thumbnailRequests[0].thumbnailType", comicRequest.getThumbnailRequests().get(0).getThumbnailType().toString().getBytes());
        MockPart comicMainThumbnailType = new MockPart("thumbnailRequests[1].thumbnailType", comicRequest.getThumbnailRequests().get(1).getThumbnailType().toString().getBytes());

        return mockMvc.perform(RestDocumentationRequestBuilders.multipart("/comics")
                .file(smallThumbnailFile)
                .file(mainThumbnailFile)
                .part(comicName)
                .part(comicGenre)
                .part(comicSummary)
                .part(comicDayOfWeek)
                .part(comicSmallThumbnailType)
                .part(comicMainThumbnailType)
                .content(RequestUtil.createMultipartRequestBody(
                        List.of(
                                comicName,
                                comicGenre,
                                comicSummary,
                                comicDayOfWeek,
                                comicSmallThumbnailType,
                                comicMainThumbnailType,
                                smallThumbnailFile,
                                mainThumbnailFile
                        )
                ))
                .session(loginSession)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );
    }

    private MockHttpSession loginUser(Long userId, LoginId loginId, UserAuthority userAuthority) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(userId, loginId, userAuthority));
        return session;
    }

    private User saveAuthorUser() {
        User user = UserDummy.createUser(UserAuthority.AUTHOR);
        Author author = AuthorDummy.createAuthor(user);
        userRepository.save(user);
        authorRepository.save(author);
        return user;
    }
}