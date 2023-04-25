package com.kongtoon.common.aws.event;

import com.kongtoon.common.aws.FileType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDeleteAfterCommitEvent {
	private final String fileUrl;
	private final FileType fileType;
}
