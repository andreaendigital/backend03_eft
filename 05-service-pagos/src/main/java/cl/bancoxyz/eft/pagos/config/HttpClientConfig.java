package cl.bancoxyz.eft.pagos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean
    RestClient cuentasRestClient(RestClient.Builder builder,
                                 @Value("${CUENTAS_SERVICE_URL:http://localhost:8081}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
