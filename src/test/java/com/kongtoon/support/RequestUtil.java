package com.kongtoon.support;

import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockPart;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

@Component
public class RequestUtil {

    @Autowired(required = false)
    private MockMvc mockMvc;

    public ResultActions requestGetWithLogin(String url, User user) throws Exception {
        return mockMvc.perform(get(url)
                .session(setSession(user)));
    }

    public ResultActions requestGetWithLoginAndParams(String url, User user, MultiValueMap<String, String> params) throws Exception {
        return mockMvc.perform(get(url)
                .params(params)
                .session(setSession(user))
        );
    }

    private MockHttpSession setSession(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, new UserAuthDTO(user.getId(), user.getLoginId(), user.getAuthority()));
        return session;
    }

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
