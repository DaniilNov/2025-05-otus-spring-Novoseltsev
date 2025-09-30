package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.otus.hw.config.SecurityConfiguration;
import ru.otus.hw.controllers.rest.AuthorRestController;
import ru.otus.hw.controllers.rest.BookRestController;
import ru.otus.hw.controllers.rest.CommentRestController;
import ru.otus.hw.controllers.rest.GenreRestController;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserService;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Тесты безопасности REST API")
@WebMvcTest(controllers = {
        BookRestController.class,
        AuthorRestController.class,
        GenreRestController.class,
        CommentRestController.class
})
@Import(SecurityConfiguration.class)
class SecurityWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserService userService;

    private MockHttpServletRequestBuilder method2RequestBuilder(String method, String url) {
        Map<String, Function<String, MockHttpServletRequestBuilder>> methodMap =
                Map.of("get", MockMvcRequestBuilders::get,
                        "post", MockMvcRequestBuilders::post,
                        "put", MockMvcRequestBuilders::put,
                        "delete", MockMvcRequestBuilders::delete);
        return methodMap.get(method).apply(url);
    }

    public static Stream<Arguments> getTestData() {
        var roles = new String[]{"USER"};
        var adminRoles = new String[]{"ADMIN"};
        return Stream.of(
                Arguments.of("get", "/api/v1/authors", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/authors", "user", roles, 200, false, false, null),
                Arguments.of("get", "/api/v1/books", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/books", "user", roles, 200, false, false, null),
                Arguments.of("get", "/api/v1/books/1", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/books/1", "user", roles, 404, false, false, null),
                Arguments.of("post", "/api/v1/books", null, null, 302, true, true, MediaType.APPLICATION_JSON),
                Arguments.of("post", "/api/v1/books", "user", roles, 403, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("put", "/api/v1/books/1", null, null, 302, true, true, MediaType.APPLICATION_JSON),
                Arguments.of("put", "/api/v1/books/1", "user", roles, 403, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("delete", "/api/v1/books/1", null, null, 302, true, true, null),
                Arguments.of("delete", "/api/v1/books/1", "user", roles, 403, false, true, null),
                Arguments.of("get", "/api/v1/genres", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/genres", "user", roles, 200, false, false, null),
                Arguments.of("get", "/api/v1/comments/book/1", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/comments/book/1", "user", roles, 200, false, false, null),
                Arguments.of("get", "/api/v1/comments/1", null, null, 302, true, false, null),
                Arguments.of("get", "/api/v1/comments/1", "user", roles, 404, false, false, null),
                Arguments.of("post", "/api/v1/comments", null, null, 302, true, true, MediaType.APPLICATION_JSON),
                Arguments.of("post", "/api/v1/comments", "user", roles, 400, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("put", "/api/v1/comments/1", null, null, 302, true, true, MediaType.APPLICATION_JSON),
                Arguments.of("delete", "/api/v1/comments/1", null, null, 302, true, true, null),
                Arguments.of("post", "/api/v1/books", "admin", adminRoles, 400, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("put", "/api/v1/books/1", "admin", adminRoles, 400, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("delete", "/api/v1/books/1", "admin", adminRoles, 204, false, true, null),
                Arguments.of("put", "/api/v1/comments/1", "admin", adminRoles, 400, false, true, MediaType.APPLICATION_JSON),
                Arguments.of("delete", "/api/v1/comments/1", "admin", adminRoles, 204, false, true, null)
        );
    }

    @DisplayName("должен возвращать ожидаемый статус")
    @ParameterizedTest(name = "{0} {1} для пользователя {2} должен возвращать статус {4}")
    @MethodSource("getTestData")
    void shouldReturnExpectedStatus(String method, String url,
                                    String userName, String[] roles,
                                    int status, boolean checkLoginRedirection,
                                    boolean needsCsrf, MediaType contentType) throws Exception {

        var request = method2RequestBuilder(method, url);

        if (nonNull(userName)) {
            request = request.with(user(userName).roles(roles));
        }

        if (needsCsrf) {
            request = request.with(csrf());
        }

        if (nonNull(contentType)) {
            request = request.contentType(contentType).content("{}");
        }

        ResultActions resultActions = mockMvc.perform(request)
                .andExpect(status().is(status));

        if (checkLoginRedirection) {
            resultActions.andExpect(redirectedUrlPattern("**/login"));
        }
    }
}