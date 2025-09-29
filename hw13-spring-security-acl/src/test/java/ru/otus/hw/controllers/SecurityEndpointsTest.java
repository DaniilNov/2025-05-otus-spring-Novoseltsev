package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.otus.hw.config.SecurityConfiguration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Тесты безопасности веб-страниц")
@WebMvcTest({
        BookController.class,
        AuthorController.class,
        GenreController.class,
        CommentController.class,
        LoginController.class,
        HomeController.class
})
@Import(SecurityConfiguration.class)
class SecurityEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpServletRequestBuilder method2RequestBuilder(String method, String url) {
        Map<String, Function<String, MockHttpServletRequestBuilder>> methodMap =
                Map.of("get", MockMvcRequestBuilders::get,
                        "post", MockMvcRequestBuilders::post);
        return methodMap.get(method).apply(url);
    }

    public static Stream<Arguments> getTestData() {
        var roles = new String[]{"USER"};
        var adminRoles = new String[]{"ADMIN"};
        return Stream.of(
                Arguments.of("get", "/", null, null, 302, true),
                Arguments.of("get", "/books", null, null, 302, true),
                Arguments.of("get", "/books/create", null, null, 302, true),
                Arguments.of("get", "/books/edit/1", null, null, 302, true),
                Arguments.of("get", "/books/view/1", null, null, 302, true),
                Arguments.of("get", "/authors", null, null, 302, true),
                Arguments.of("get", "/genres", null, null, 302, true),
                Arguments.of("get", "/comments/view/1", null, null, 302, true),
                Arguments.of("get", "/comments/create", null, null, 302, true),
                Arguments.of("get", "/comments/edit/1", null, null, 302, true),
                Arguments.of("post", "/perform_logout", null, null, 302, false),

                Arguments.of("get", "/", "user", roles, 302, false),
                Arguments.of("get", "/books", "user", roles, 200, false),
                Arguments.of("get", "/books/create", "user", roles, 403, false),
                Arguments.of("get", "/books/edit/1", "user", roles, 403, false),
                Arguments.of("get", "/authors", "user", roles, 200, false),
                Arguments.of("get", "/genres", "user", roles, 200, false),
                Arguments.of("post", "/perform_logout", "user", roles, 302, false),

                Arguments.of("get", "/login", null, null, 200, false),
                Arguments.of("get", "/login", "user", roles, 200, false),

                Arguments.of("get", "/books/create", "admin", adminRoles, 200, false),
                Arguments.of("get", "/books/edit/1", "admin", adminRoles, 200, false)
        );
    }

    @DisplayName("должен возвращать ожидаемый статус")
    @ParameterizedTest(name = "{0} {1} для пользователя {2} должен возвращать статус {4}")
    @MethodSource("getTestData")
    void shouldReturnExpectedStatus(String method, String url,
                                    String userName, String[] roles,
                                    int status, boolean checkLoginRedirection) throws Exception {

        var request = method2RequestBuilder(method, url);

        if (nonNull(userName)) {
            request = request.with(user(userName).roles(roles));
        }

        if (method.equals("post")) {
            request = request.with(csrf());
        }

        ResultActions resultActions = mockMvc.perform(request)
                .andExpect(status().is(status));

        if (checkLoginRedirection) {
            resultActions.andExpect(redirectedUrlPattern("**/login"));
        }
    }
}