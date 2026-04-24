package com.example.stmgt.config;

import com.example.stmgt.security.CustomUserDetailsService;
import com.example.stmgt.security.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        CustomUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        RoleBasedAuthenticationSuccessHandler successHandler
    ) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieHttpOnly(false);
        csrfTokenRepository.setCookieName("csrftoken");
        csrfTokenRepository.setParameterName("csrfmiddlewaretoken");
        csrfTokenRepository.setHeaderName("X-CSRFToken");

        http
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/", "/error", "/users", "/users/", "/users/login", "/users/login/").permitAll()

                .requestMatchers("/users/admin/**").hasRole("ADMIN")
                .requestMatchers("/courses/**").hasRole("ADMIN")
                .requestMatchers(
                    "/faculty/api/user-email/**",
                    "/faculty/faculty_list/**",
                    "/faculty/*/manage/**",
                    "/faculty/delete/**",
                    "/faculty/remove-course/**"
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/users/students/*/delete/",
                    "/users/faculty/*/delete/",
                    "/users/courses/*/delete/",
                    "/students/export/**"
                ).hasRole("ADMIN")
                .requestMatchers("/students/*/delete/").hasRole("ADMIN")

                .requestMatchers("/users/faculty/dashboard/**").hasRole("FACULTY")
                .requestMatchers("/users/faculty/courses/*/grades/").hasRole("FACULTY")
                .requestMatchers("/users/faculty/grades/*/assign/").hasRole("FACULTY")

                .requestMatchers("/users/student/dashboard/**", "/users/student/grades/**").hasRole("STUDENT")
                .requestMatchers("/tasks/*/update-progress/").hasRole("STUDENT")

                .requestMatchers("/students/*/export/**").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers("/students/**").hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers("/tasks/**").hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers("/faculty/manage-tasks/**").hasAnyRole("ADMIN", "FACULTY")

                .requestMatchers("/users/logout/").authenticated()
                .requestMatchers("/change-password/", "/admin", "/admin/", "/st_mgt/admin", "/st_mgt/admin/")
                    .authenticated()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
            .formLogin(form -> form
                .loginPage("/users/login/")
                .loginProcessingUrl("/users/login/")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/users/login/?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new OrRequestMatcher(
                    new AntPathRequestMatcher("/users/logout/", "GET"),
                    new AntPathRequestMatcher("/users/logout/", "POST")
                ))
                .logoutSuccessUrl("/users/login/?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
