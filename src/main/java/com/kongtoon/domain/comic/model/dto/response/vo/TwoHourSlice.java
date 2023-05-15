package com.kongtoon.domain.comic.model.dto.response.vo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum TwoHourSlice {
	HOUR_00_02(LocalTime.of(0, 0), LocalTime.of(2, 0)),
	HOUR_02_04(LocalTime.of(2, 0), LocalTime.of(4, 0)),
	HOUR_04_06(LocalTime.of(4, 0), LocalTime.of(6, 0)),
	HOUR_06_08(LocalTime.of(6, 0), LocalTime.of(8, 0)),
	HOUR_08_10(LocalTime.of(8, 0), LocalTime.of(10, 0)),
	HOUR_10_12(LocalTime.of(10, 0), LocalTime.of(12, 0)),
	HOUR_12_14(LocalTime.of(12, 0), LocalTime.of(14, 0)),
	HOUR_14_16(LocalTime.of(14, 0), LocalTime.of(16, 0)),
	HOUR_16_18(LocalTime.of(16, 0), LocalTime.of(18, 0)),
	HOUR_18_20(LocalTime.of(18, 0), LocalTime.of(20, 0)),
	HOUR_20_22(LocalTime.of(20, 0), LocalTime.of(22, 0)),
	HOUR_22_24(LocalTime.of(22, 0), LocalTime.of(23, 59, 59));

	private final LocalTime startTime;
	private final LocalTime endTime;

	TwoHourSlice(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public LocalDate getPrevSliceDate() {
		if (this == HOUR_22_24) {
			return LocalDate.now().minusDays(1);
		}
		return LocalDate.now();
	}

	public static TwoHourSlice getPrev(LocalTime now) {
		for (int i = 0; i < values().length; i++) {
			if (now.isAfter(values()[i].startTime) && now.isBefore(values()[i].endTime)) {
				if (values()[i] == HOUR_00_02) {
					return HOUR_22_24;
				}
				return values()[i - 1];
			}
		}

		throw new BusinessException(ErrorCode.INCORRECT_TIME);
	}

	public static TwoHourSlice getNow(LocalTime now) {
		return Arrays.stream(values())
				.filter(slice -> slice.startTime.isBefore(now) && slice.endTime.isAfter(now))
				.findFirst()
				.orElseThrow(() -> new BusinessException(ErrorCode.INCORRECT_TIME));
	}

	public TwoHourSlice getNext() {
		for (int i = 0; i < values().length; i++) {
			if (values()[i] == this) {
				if (values()[i] == HOUR_22_24) {
					return HOUR_00_02;
				}
				return values()[i + 1];
			}
		}

		throw new BusinessException(ErrorCode.INCORRECT_TIME);
	}
}
