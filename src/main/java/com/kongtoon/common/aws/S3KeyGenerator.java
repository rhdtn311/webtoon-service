package com.kongtoon.common.aws;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3KeyGenerator {

	private static final String SEPARATOR = "_";

	public static String makeKey(String originalFileName, FileType fileType) {
		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"));

		return fileType.getPath()
				+ currentTime
				+ SEPARATOR
				+ originalFileName;
	}
}