package com.kongtoon.common.aws;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

	String upload(MultipartFile multipartFile, FileType fileType);

	void delete(String key, FileType fileType);
}