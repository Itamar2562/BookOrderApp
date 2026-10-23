package com.itamarprojects.bookorderapp.config;

import com.itamarprojects.bookorderapp.users.Permissions;
import com.itamarprojects.bookorderapp.users.UserRole;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter , AuthenticationProvider authenticationProvider){
        this.jwtAuthFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    private static final String[] WHITE_LIST_URL =
            {
                    "/api/users/register",
                    "/api/users/login",
                    "/home"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception
    {


        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                                .hasAnyAuthority("ROLE_MANAGER",
                                        Permissions.WORKER_READ.getPermission())
                        .requestMatchers("/api/orders", "/api/orders/**").hasAuthority("ROLE_MANAGER")
                         .requestMatchers("/api/books").hasAnyAuthority("ROLE_MANAGER","ROLE_WORKER")
                         .requestMatchers("/api/users").hasAnyAuthority("ROLE_MANAGER" , "ROLE_WORKER")
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint((request, response, authException) ->
                                response.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
