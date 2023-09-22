package com.kongtoon.domain.comic.service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.FileType;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteAfterRollbackEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.support.dummy.AuthorDummy;
import com.kongtoon.support.dummy.ComicDummy;
import com.kongtoon.support.dummy.ThumbnailDummy;
import com.kongtoon.support.dummy.UserDummy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComicModifyServiceTest {

    @InjectMocks
    ComicModifyService comicModifyService;

    @Mock
    ComicRepository comicRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthorRepository authorRepository;

    @Mock
    ThumbnailRepository thumbnailRepository;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    FileStorage fileStorage;

    @Test
    @DisplayName("웬툰 생성에 성공한다.")
    void createComicSuccess() throws IOException {

        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        User user = UserDummy.createUser();
        Author author = AuthorDummy.createAuthor(user);
        String uploadedThumbnailImageUrl1 = "uploadedThumbnailImageUrl1";
        String uploadedThumbnailImageUrl2 = "uploadedThumbnailImageUrl2";
        Comic comic = ComicDummy.createComic(comicRequest.getComicName(), author);
        Thumbnail thumbnail = ThumbnailDummy.createSmallTypeThumbnail(uploadedThumbnailImageUrl1, comic);

        when(userRepository.findByLoginId(user.getLoginId()))
                .thenReturn(Optional.of(user));
        when(authorRepository.findByUser(user))
                .thenReturn(Optional.of(author));
        when(comicRepository.save(any(Comic.class)))
                .thenReturn(comic);
        when(fileStorage.upload(comicRequest.getThumbnailRequests().get(0).getThumbnailImage(), ImageFileType.COMIC_THUMBNAIL))
                .thenReturn(uploadedThumbnailImageUrl1);
        when(fileStorage.upload(comicRequest.getThumbnailRequests().get(1).getThumbnailImage(), ImageFileType.COMIC_THUMBNAIL))
                .thenReturn(uploadedThumbnailImageUrl2);
        when(thumbnailRepository.save(any(Thumbnail.class)))
                .thenReturn(thumbnail);
        doNothing()
                .when(applicationEventPublisher).publishEvent(
                        any(FileDeleteAfterRollbackEvent.class)
                );

        // when
        comicModifyService.createComic(comicRequest, user.getLoginId());

        // then
        verify(comicRepository).save(any(Comic.class));
        verify(fileStorage).upload(comicRequest.getThumbnailRequests().get(0).getThumbnailImage(), ImageFileType.COMIC_THUMBNAIL);
        verify(fileStorage).upload(comicRequest.getThumbnailRequests().get(1).getThumbnailImage(), ImageFileType.COMIC_THUMBNAIL);
        verify(thumbnailRepository, times(2)).save(any(Thumbnail.class));
        verify(applicationEventPublisher, times(2)).publishEvent(any(FileDeleteAfterRollbackEvent.class));
    }

    @Test
    @DisplayName("웬툰 생성 시 요청하는 사용자가 존재하지 않아 실패한다.")
    void createComicNotExistsUserFail() throws IOException {

        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        LoginId loginId = new LoginId("loginId");

        when(userRepository.findByLoginId(loginId))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> comicModifyService.createComic(comicRequest, loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("웬툰 생성 시 요청하는 작가가 존재하지 않아 실패한다.")
    void createComicNotExistsAuthorFail() throws IOException {

        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        LoginId loginId = new LoginId("loginId");
        Email email = new Email("email@email.com");
        User user = UserDummy.createUser(email, loginId);

        when(userRepository.findByLoginId(loginId))
                .thenReturn(Optional.of(user));
        when(authorRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> comicModifyService.createComic(comicRequest, loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHOR_NOT_FOUND);

        verify(userRepository).findByLoginId(loginId);
    }

    @Test
    @DisplayName("웬툰 생성 시 파일 저장소 업로드 문제로 실패한다.")
    void createComicNotUploadedFileFail() throws IOException {

        // given
        ComicRequest comicRequest = ComicDummy.createComicRequest();
        LoginId loginId = new LoginId("loginId");
        Email email = new Email("email@email.com");
        User user = UserDummy.createUser(email, loginId);
        Author author = AuthorDummy.createAuthor(user);

        when(userRepository.findByLoginId(loginId))
                .thenReturn(Optional.of(user));
        when(authorRepository.findByUser(user))
                .thenReturn(Optional.of(author));
        when(fileStorage.upload(any(MultipartFile.class), any(FileType.class)))
                .thenThrow(new BusinessException(ErrorCode.FILE_NOT_UPLOAD));

        // when, then
        assertThatThrownBy(() -> comicModifyService.createComic(comicRequest, loginId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_UPLOAD);

        verify(userRepository).findByLoginId(loginId);
        verify(authorRepository).findByUser(user);
    }

}