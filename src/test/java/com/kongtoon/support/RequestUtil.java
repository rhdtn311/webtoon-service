package com.kongtoon.support;

import org.springframework.mock.web.MockPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class RequestUtil {

    public static <T> String createMultipartRequestBody(List<T> parts) throws IOException {
        StringBuilder requestPartBody = new StringBuilder();
        String boundary = "----------------------------boundary";
        String newLine = System.lineSeparator();

        for (T part : parts) {
            if (part instanceof MultipartFile file) {
                requestPartBody.append(boundary)
                        .append(newLine)
                        .append("Content-Disposition: form-data; ")
                        .append("name= \"").append(file.getName()).append("\"; ")
                        .append("filename=\"").append(file.getOriginalFilename()).append("\"")
                        .append(newLine)
                        .append("Content-Type: ").append(file.getContentType())
                        .append(newLine)
                        .append(newLine)
                        .append("binary file data")
                        .append(newLine);
            } else if (part instanceof MockPart mockPart) {
                requestPartBody.append(boundary)
                        .append(newLine)
                        .append("Content-Disposition: form-data; ")
                        .append("name= \"").append(mockPart.getName()).append("\"; ")
                        .append(newLine)
                        .append(newLine)
                        .append(new String(mockPart.getInputStream().readAllBytes()))
                        .append(newLine);
            }
        }

        return requestPartBody.toString();
    }
}
