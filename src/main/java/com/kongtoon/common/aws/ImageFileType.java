package com.kongtoon.common.aws;

import lombok.Getter;

@Getter
public enum ImageFileType implements FileType {
	EPISODE("images/episodes/"),
	EPISODE_THUMBNAIL("images/episode_thumbnails/"),
	COMIC_THUMBNAIL("images/comic_thumbnails/"),
	ETC("images/etc/");

	private final String path;

	ImageFileType(String path) {
		this.path = path;
	}
}