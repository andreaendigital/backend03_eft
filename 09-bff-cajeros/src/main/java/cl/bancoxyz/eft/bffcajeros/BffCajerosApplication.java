package cl.bancoxyz.eft.bffcajeros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BffCajerosApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffCajerosApplication.class, args);
    }
}
