package com.example.Test.Config;

import com.example.Test.Service.AppUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserService appUserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public SecurityConfig(AppUserService appUserService,
                          CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                          CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.appUserService = appUserService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());  // {noop} encoder
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authenticationProvider());
        return auth.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/signup", "/Forgotpassword", "/passwordresetform" ,"/api/passwordresetrequest", "/api/resetpassword").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler)  // Custom success handler
                        .failureHandler(customAuthenticationFailureHandler)  // Custom failure handler
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
