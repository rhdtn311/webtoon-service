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
	public void deleteFileAfterRollback(FileDeleteAfterRollbackEvent fileDeleteAfterRollbackEvent) {
		fileStorage.delete(fileDeleteAfterRollbackEvent.getFileUrl(), ImageFileType.COMIC_THUMBNAIL);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteFileAfterCommit(FileDeleteAfterCommitEvent fileDeleteAfterRollbackEvent) {
		fileStorage.delete(fileDeleteAfterRollbackEvent.getFileUrl(), ImageFileType.COMIC_THUMBNAIL);
	}
}
