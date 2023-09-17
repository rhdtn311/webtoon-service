package com.kongtoon.utils;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.*;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.*;
import com.kongtoon.domain.view.model.View;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TestUtil {

	public static SignupRequest createSignupRequest() {
		return new SignupRequest(
				new LoginId("loginId"),
				"Name",
				new Email("email@email.com"),
				"nickname",
				new Password("password123!")
		);
	}

	public static SignupRequest createSignupRequest(LoginId loginId, Email email) {
		return new SignupRequest(
				loginId,
				"Name",
				email,
				"nickname",
				new Password("password123!")
		);
	}

	public static User createUser(Email email, LoginId loginId) {
		return new User(
				loginId,
				"Name",
				email,
				"nickname",
				new Password("password123!"),
				UserAuthority.USER,
				true
		);
	}

	public static User createUser(Email email, LoginId loginId, Password password) {
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

	public static User createUser(UserAuthority userAuthority) {
		return new User(
				new LoginId("loginId"),
				"Name",
				new Email("email@email.com"),
				"nickname",
				new Password("password123!"),
				userAuthority,
				true
		);
	}

	public static LoginRequest createLoginRequest() {
		return new LoginRequest(
				new LoginId("loginId"), new Password("password123!")
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

		return new MockMultipartFile("mock_image_file.png",
				"mock_image_file.png",
				"image/png",
				new FileInputStream("src/test/resources/file/mock_image_file.png"));
	}

	public static MockMultipartFile createMockMultipartFile(String name, String originalFilename, String contentType) throws IOException {

		return new MockMultipartFile(name,
				originalFilename,
				contentType,
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

	public static Episode createEpisode(String title, int episodeNumber, String thumbnailUrl, Comic comic) {
		return new Episode(title, episodeNumber, thumbnailUrl, comic);
	}

	public static View createView(User user, Episode episode) {
		return new View(user, episode);
	}

	public static Thumbnail createThumbnail(ThumbnailType thumbnailType, String imageUrl, Comic comic) {
		return new Thumbnail(
				thumbnailType, imageUrl, comic);
	}

	public static List<ComicByGenreResponse> createActionGenreComics() {
		return List.of(
				new ComicByGenreResponse(1L, "name1", "author1", "thumbnailUrl1", true, 1),
				new ComicByGenreResponse(2L, "name2", "author2", "thumbnailUrl2", false, 5),
				new ComicByGenreResponse(3L, "name3", "author3", "thumbnailUrl3", false, 3)
		);
	}

	public static <T> String createMultipartRequestBody(List<T> parts) throws IOException {
		StringBuilder requestPartBody = new StringBuilder();
		String boundary = "----------------------------boundary";
		String newLine = System.lineSeparator();

		for (T part : parts) {
			if (part instanceof MultipartFile file) {
				requestPartBody.append(boundary)
						.append(newLine)
						.append("Content-Disposition: form-data; ")
						.append("name= \"").append(file.getName()).append("\"; ")
						.append("filename=\"").append(file.getOriginalFilename()).append("\"")
						.append(newLine)
						.append("Content-Type: ").append(file.getContentType())
						.append(newLine)
						.append(newLine)
						.append("binary file data")
						.append(newLine);
			} else if (part instanceof MockPart mockPart) {
				requestPartBody.append(boundary)
						.append(newLine)
						.append("Content-Disposition: form-data; ")
						.append("name= \"").append(mockPart.getName()).append("\"; ")
						.append(newLine)
						.append(newLine)
						.append(new String(mockPart.getInputStream().readAllBytes()))
						.append(newLine);
			}
		}

		return requestPartBody.toString();
	}
}
