package com.kongtoon.domain.comic.model;

public enum ThumbnailType {
	SMALL, MAIN;

	public boolean isSameType(ThumbnailType thumbnailType) {
		return this == thumbnailType;
	}
}
