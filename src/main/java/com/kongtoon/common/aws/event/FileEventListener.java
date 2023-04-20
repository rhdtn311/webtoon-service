package com.kongtoon.common.aws.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.ImageFileType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileEventListener {

	private final FileStorage fileStorage;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void deleteFile(FileDeleteEvent fileDeleteEvent) {
		fileStorage.delete(fileDeleteEvent.getKey(), ImageFileType.COMIC_THUMBNAIL);
	}
}
