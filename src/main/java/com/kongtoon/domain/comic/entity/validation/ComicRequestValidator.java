package com.kongtoon.domain.comic.entity.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.kongtoon.domain.comic.entity.ThumbnailType;
import com.kongtoon.domain.comic.entity.dto.request.ComicRequest;
import com.kongtoon.domain.comic.entity.dto.request.ComicRequest.ThumbnailRequest;

@Component
public class ComicRequestValidator implements ConstraintValidator<ComicRequestValid, ComicRequest> {

	private static final String THUMBNAIL_INFO_REQUEST_FIRST_FILED = "thumbnailRequests[]";
	private static final String THUMBNAIL_TYPE_REQUEST_SECOND_FILED = "thumbnailType";

	private static final String NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE = "모든 종류의 썸네일 타입을 보내야합니다.";

	@Override
	public boolean isValid(ComicRequest comicRequest, ConstraintValidatorContext context) {
		boolean isValid = true;
		List<ThumbnailRequest> thumbnailRequests = comicRequest.getThumbnailRequests();

		if (!hasAllThumbnailType(thumbnailRequests)) {
			addConstraintViolation(context,
					NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE,
					THUMBNAIL_INFO_REQUEST_FIRST_FILED,
					THUMBNAIL_TYPE_REQUEST_SECOND_FILED
			);
			isValid = false;
		}

		return isValid;
	}

	private boolean hasAllThumbnailType(List<ThumbnailRequest> thumbnailRequests) {
		Map<ThumbnailType, Long> thumbnailTypes = thumbnailRequests.stream()
				.collect(Collectors.groupingBy(ThumbnailRequest::getThumbnailType, Collectors.counting()));

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
