package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfiguration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @ParameterizedTest
    @ValueSource(strings = {
            "/books", "/books/create", "/books/edit/1", "/books/view/1",
            "/authors",
            "/genres",
            "/comments/view/1", "/comments/create?bookId=1", "/comments/edit/1"
    })
    @DisplayName("GET-запросы к защищенным страницам должны перенаправлять анонимных пользователей на логин")
    void protectedPagesShouldRedirectToLoginForAnonymousUsers(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/books", "/books/create", "/authors", "/genres"})
    @DisplayName("GET-запросы к защищенным страницам должны быть доступны аутентифицированным пользователям")
    @WithMockUser
    void protectedPagesShouldBeAccessibleForAuthenticatedUsers(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/perform_logout"})
    @DisplayName("POST-запросы к /perform_logout должны перенаправлять анонимных пользователей на логин")
    void logoutEndpointShouldRedirectAnonymousUsers(String url) throws Exception {
        mockMvc.perform(post(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/perform_logout"})
    @DisplayName("POST-запросы к защищенным эндпоинтам должны быть доступны аутентифицированным пользователям")
    @WithMockUser
    void protectedPostEndpointsShouldBeAccessibleForAuthenticatedUsers(String url) throws Exception {
        mockMvc.perform(post(url).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Страница логина должна быть доступна всем")
    void loginPageShouldBeAccessibleToAll() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}