package cl.bancoxyz.eft.bffweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth.pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(oauth2 -> { })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> { }))
                .build();
    }
}
