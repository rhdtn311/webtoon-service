package com.kongtoon.support;

import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

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
}
