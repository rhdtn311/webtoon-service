package com.kongtoon.support.dummy;

import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

public class FileDummy {
    public static MockMultipartFile createMockMultipartFile() throws IOException {

        return new MockMultipartFile("mock_image_file.png",
                "mock_image_file.png",
                "image/png",
                new FileInputStream("src/test/resources/file/mock_image_file.png"));
    }

    public static MockMultipartFile createMockMultipartFile(String name, String originalFilename, String contentType) throws IOException {

        return new MockMultipartFile(name,
                originalFilename,
                contentType,
                new FileInputStream("src/test/resources/file/mock_image_file.png"));
    }
}
