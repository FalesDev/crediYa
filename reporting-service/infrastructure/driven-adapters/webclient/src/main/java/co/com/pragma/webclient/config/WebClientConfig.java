package co.com.pragma.webclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final String requestServiceUrl;
    private final String authServiceUrl;

    public WebClientConfig (
            @Value("${services.request.url}") String requestServiceUrl,
            @Value("${services.auth.url}") String authServiceUrl) {
        this.requestServiceUrl = requestServiceUrl;
        this.authServiceUrl = authServiceUrl;
    }

    @Bean
    public WebClient requestWebClient() {
        return WebClient.builder()
                .baseUrl(requestServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient authWebClient() {
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
