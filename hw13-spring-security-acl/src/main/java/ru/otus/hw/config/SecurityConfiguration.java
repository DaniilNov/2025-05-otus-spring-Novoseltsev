package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureAuthorizationRules(http);
        configureFormLogin(http);
        configureLogout(http);
        configureCsrf(http);

        return http.build();
    }

    private void configureAuthorizationRules(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books", "/books/view/**", "/authors", "/genres")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/books/create", "/books/edit/**").hasRole("ADMIN")
                        .requestMatchers("/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/books", "/api/v1/books/**",
                                "/api/v1/authors", "/api/v1/authors/**", "/api/v1/genres", "/api/v1/genres/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/books", "/api/v1/authors", "/api/v1/genres")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**", "/api/v1/authors/**", "/api/v1/genres/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**", "/api/v1/authors/**",
                                "/api/v1/genres/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/comments").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                );
    }

    private void configureFormLogin(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/books", true)
                        .permitAll()
                );
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/**")
                );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}