package com.kongtoon.utils;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.*;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TestUtil {

	public static SignupRequest createSignupRequest() {
		return new SignupRequest(
				"loginId",
				"Name",
				"email@email.com",
				"nickname",
				"password123!"
		);
	}

	public static SignupRequest createSignupRequest(String loginId, String email) {
		return new SignupRequest(
				loginId,
				"Name",
				email,
				"nickname",
				"password123!"
		);
	}

	public static User createUser(String email, String loginId) {
		return new User(
				loginId,
				"Name",
				email,
				"nickname",
				"password123!",
				UserAuthority.USER,
				true
		);
	}

	public static User createUser(String email, String loginId, String password) {
		return new User(
				loginId,
				"Name",
				email,
				"nickname",
				password,
				UserAuthority.USER,
				true
		);
	}

	public static LoginRequest createLoginRequest() {
		return new LoginRequest(
				"loginId", "password"
		);
	}

	public static ComicRequest createComicRequest() throws IOException {
		ComicRequest comicRequest = new ComicRequest();

		comicRequest.setComicName("comic name");
		comicRequest.setGenre(Genre.ACTION);
		comicRequest.setSummary("summary");
		comicRequest.setPublishDayOfWeek(PublishDayOfWeek.FRI);

		ComicRequest.ThumbnailRequest smallTypeThumbnail = createThumbnailRequest(ThumbnailType.SMALL);
		ComicRequest.ThumbnailRequest mainTypeThumbnail = createThumbnailRequest(ThumbnailType.MAIN);

		comicRequest.setThumbnailRequests(List.of(smallTypeThumbnail, mainTypeThumbnail));

		return comicRequest;
	}

	private static ComicRequest.ThumbnailRequest createThumbnailRequest(ThumbnailType thumbnailType) throws IOException {
		ComicRequest.ThumbnailRequest thumbnailRequest = new ComicRequest.ThumbnailRequest();
		thumbnailRequest.setThumbnailType(thumbnailType);
		thumbnailRequest.setThumbnailImage(createMockMultipartFile());

		return thumbnailRequest;
	}

	public static MockMultipartFile createMockMultipartFile() throws IOException {

		return new MockMultipartFile("mock_image_file",
				"mock_image_file.png",
				"image/png",
				new FileInputStream("src/test/resources/file/mock_image_file.png"));
	}

	public static Author createAuthor(User user) {
		return new Author(
				"author name",
				"introduction",
				"belong",
				user
		);
	}

	public static Comic createComic(String name, Genre genre, String summary, PublishDayOfWeek publishDayOfWeek, Author author) {
		return new Comic(
				name, genre, summary, publishDayOfWeek, author);
	}

	public static Thumbnail createThumbnail(ThumbnailType thumbnailType, String imageUrl, Comic comic) {
		return new Thumbnail(
				thumbnailType, imageUrl, comic);
	}
}
