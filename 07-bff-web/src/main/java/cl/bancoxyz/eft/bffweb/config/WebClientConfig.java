package cl.bancoxyz.eft.bffweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    WebClient webClient(WebClient.Builder builder) {
        return builder.filter((request, next) -> ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(authentication -> ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(
                                ((JwtAuthenticationToken) authentication).getToken().getTokenValue()))
                        .build())
                .defaultIfEmpty(request)
                .flatMap(next::exchange)).build();
    }
}
