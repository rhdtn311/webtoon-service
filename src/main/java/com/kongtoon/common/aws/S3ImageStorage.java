package com.kongtoon.common.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3ImageStorage implements FileStorage {

	private static final String AWS_S3_ADDRESS_FORMAT = "https://s3-%s.amazonaws.com/%s/%s";
	private static final List<String> permitExtensions = List.of("jpeg", "jpg", "png");

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.region.static}")
	private String region;

	private final AmazonS3 amazonS3;

	public String upload(MultipartFile multipartFile, FileType fileType) {
		String originalFilename = multipartFile.getOriginalFilename();

		verifyExtension(getExtension(originalFilename));

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(multipartFile.getContentType());
		objectMetadata.setContentLength(multipartFile.getSize());

		String key = S3KeyGenerator.makeKey(originalFilename, fileType);

		try (InputStream inputStream = multipartFile.getInputStream()) {
			amazonS3.putObject(
					bucket,
					key,
					inputStream,
					objectMetadata
			);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.FILE_NOT_UPLOAD);
		}

		return getSavedFileUrl(key);
	}

	private void verifyExtension(String extension) {
		if (!permitExtensions.contains(extension)) {
			throw new BusinessException(ErrorCode.NOT_ALLOWED_EXTENSION);
		}
	}

	public void delete(String fileUrl, FileType fileType) {
		try {
			String key = getKeyContainsPath(fileUrl, fileType);
			amazonS3.deleteObject(bucket, key);
		} catch (AmazonServiceException e) {
			throw new BusinessException(ErrorCode.FILE_NOT_UPLOAD);
		}
	}

	private String getExtension(String originalFileName) {
		if (originalFileName == null) {
			throw new IllegalArgumentException("파일에 확장자가 존재하지 않습니다.");
		}

		int extensionSeparatorIndex = originalFileName.lastIndexOf(".") + 1;

		return originalFileName.substring(extensionSeparatorIndex);
	}

	private String getSavedFileUrl(String key) {
		return String.format(AWS_S3_ADDRESS_FORMAT, region, bucket, key);
	}

	private String getKeyContainsPath(String fileUrl, FileType fileType) {
		return fileType.getPath()
				+ fileUrl.substring(
				fileUrl.lastIndexOf("/") + 1
		);
	}
}