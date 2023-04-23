package com.kongtoon.domain.comic.entity;

public enum ThumbnailType {
	SMALL, MAIN;

	public boolean isSameType(ThumbnailType thumbnailType) {
		return this == thumbnailType;
	}
}
