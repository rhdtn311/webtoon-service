package com.kongtoon.domain.episode.model.validation;

import static com.kongtoon.domain.episode.model.dto.request.EpisodeRequest.*;

import java.util.List;
import java.util.stream.IntStream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;

@Component
public class EpisodeRequestValidator implements ConstraintValidator<EpisodeRequestValid, EpisodeRequest> {

	private static final String EPISODE_CONTENT_REQUESTS_FIELD = "episodeContentRequests[]";
	private static final String EPISODE_CONTENT_REQUESTS_ORDER_FIELD = "contentOrder";

	private static final String NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE = "에피소드 이미지는 0부터 순서대로 보내주세요.";

	@Override
	public boolean isValid(EpisodeRequest episodeRequest, ConstraintValidatorContext context) {
		boolean isValid = true;

		if (!hasCorrectImageOrder(episodeRequest.getEpisodeContentRequests())) {
			addConstraintViolation(context,
					NOT_HAS_ALL_THUMBNAIL_TYPES_MESSAGE,
					EPISODE_CONTENT_REQUESTS_FIELD,
					EPISODE_CONTENT_REQUESTS_ORDER_FIELD
			);

			isValid = false;
		}

		return isValid;
	}

	private boolean hasCorrectImageOrder(List<EpisodeContentRequest> episodeContentRequests) {
		int orderRequestSize = episodeContentRequests.size();

		List<Integer> imageOrders = episodeContentRequests.stream()
				.map(EpisodeContentRequest::getContentOrder)
				.toList();

		long correctOrderCount = IntStream.range(0, orderRequestSize)
				.filter(imageOrders::contains)
				.count();

		return correctOrderCount == orderRequestSize;
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
