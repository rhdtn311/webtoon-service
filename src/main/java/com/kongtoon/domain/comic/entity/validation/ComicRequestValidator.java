package com.kongtoon.domain.comic.entity.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.kongtoon.domain.comic.entity.ThumbnailType;
import com.kongtoon.domain.comic.entity.dto.request.ComicCreateRequest;
import com.kongtoon.domain.comic.entity.dto.request.ComicCreateRequest.ThumbnailCreateRequest;

@Component
public class ComicRequestValidator implements ConstraintValidator<ComicRequestValid, ComicCreateRequest> {

	private static final String THUMBNAIL_INFO_REQUEST_FIRST_FILED = "thumbnailCreateRequests[]";
	private static final String THUMBNAIL_TYPE_REQUEST_SECOND_FILED = "thumbnailType";

	private static final String NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE = "모든 종류의 썸네일 타입을 보내야합니다.";

	@Override
	public boolean isValid(ComicCreateRequest comicCreateRequest, ConstraintValidatorContext context) {
		boolean isValid = true;
		List<ThumbnailCreateRequest> thumbnailCreateRequests = comicCreateRequest.getThumbnailCreateRequests();

		if (!hasAllThumbnailType(thumbnailCreateRequests)) {
			addConstraintViolation(context,
					NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE,
					THUMBNAIL_INFO_REQUEST_FIRST_FILED,
					THUMBNAIL_TYPE_REQUEST_SECOND_FILED
			);
			isValid = false;
		}

		return isValid;
	}

	private boolean hasAllThumbnailType(List<ThumbnailCreateRequest> thumbnailCreateRequests) {
		Map<ThumbnailType, Long> thumbnailTypes = thumbnailCreateRequests.stream()
				.collect(Collectors.groupingBy(ThumbnailCreateRequest::getThumbnailType, Collectors.counting()));

		for (ThumbnailType thumbnailType : ThumbnailType.values()) {
			if (!thumbnailTypes.containsKey(thumbnailType) || thumbnailTypes.get(thumbnailType) != 1) {
				return false;
			}
		}

		return true;
	}

	private void addConstraintViolation(
			ConstraintValidatorContext context,
			String errorMessage,
			String firstValue,
			String secondValue
	) {
		context.buildConstraintViolationWithTemplate(errorMessage)
				.addPropertyNode(firstValue)
				.addPropertyNode(secondValue)
				.addConstraintViolation();
	}
}
