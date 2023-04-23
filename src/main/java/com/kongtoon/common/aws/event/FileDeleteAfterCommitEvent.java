package com.kongtoon.common.aws.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDeleteAfterCommitEvent {
	private final String fileUrl;
}
