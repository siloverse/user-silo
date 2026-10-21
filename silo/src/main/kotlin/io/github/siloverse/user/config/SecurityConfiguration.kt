package io.github.siloverse.user.config

import io.github.siloverse.user.security.KeycloakJwtAuthenticationConverter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                it.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                it.requestMatchers(
                    "/actuator/health",
                    "/actuator/prometheus",
                    "/actuator/info",
                    "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/**"   // the OpenAPI contract is public: generated clients are built from it
                ).permitAll()
                it.requestMatchers("/api/users").hasRole("SYSTEM")
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { rs -> rs.jwtAuthenticationConverter(KeycloakJwtAuthenticationConverter()) }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .build()
    }
}