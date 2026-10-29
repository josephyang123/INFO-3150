package ca.kpu.team4.smart_home_energy_management.security;

import org.springframework.security.config.Customizer;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import ca.kpu.team4.smart_home_energy_management.service.UserService;

@Configuration 
public class SecurityConfig {

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http
        ,UserService userService
        ,PasswordEncoder passwordEncoder)throws Exception {

            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService);
            authProvider.setPasswordEncoder(passwordEncoder);

            http
                .authenticationProvider(authProvider)
                .csrf(Customizer.withDefaults())
                .sessionManagement(session-> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .requestCache(cache-> cache.disable())
                .authorizeHttpRequests(auth-> auth
                    .requestMatchers(
                        "/",
                        "/html/index.html",
                        "/html/login.html",
                        "/html/register.html",
                        "/css/**",
                        "/js/**",
                        "/error"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/register").permitAll()

                    .requestMatchers("/html/dashboard.html").hasRole("ADMIN")
                    .requestMatchers("/html/family-dashboard.html").hasRole("FAMILY")
                    .requestMatchers("/html/homeowner-dashboard.html").hasRole("HOMEOWNER")

                    .anyRequest().authenticated()
                )

                .formLogin(form-> form
                    .loginPage("/html/login.html")
                    .loginProcessingUrl("/api/login")
                    .usernameParameter("email")
                    .passwordParameter("password")

                    .successHandler((rquest, response, authentication)->{
                        response.setStatus(204);
                    })

                    .failureHandler((request, response, exception)->{
                        response.setStatus(401);
                    })

                    .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")

                        .logoutSuccessHandler(
                                (request, response, authentication) -> {
                                    response.setStatus(204);
                                }
                        )

                        .permitAll()
                )

                .exceptionHandling(errors -> errors
                    .authenticationEntryPoint(
                        (request, response, exception) -> {
                            if (request.getServletPath()
                                    .startsWith("/api/")) {
                                response.setStatus(401);
                            } else {
                                response.sendRedirect(
                                        request.getContextPath()
                                                + "/html/login.html"
                                );
                            }
                        }
                    )
                );

                return http.build();
    }
    
    
}
