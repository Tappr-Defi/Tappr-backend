package com.semicolon.africa.tapprbackend.reciepts.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import javax.naming.ServiceUnavailableException;
import java.time.Duration;

@Component
public class WalletApiClient {
    private static final Logger log = LoggerFactory.getLogger(WalletApiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.node-service.generate-wallet-url}")
    private String walletsEndpoint;

    public WalletApiClient(WebClient.Builder webClientBuilder,
                           @Value("${app.node-service.generate-wallet-url}") String baseUrl,
                           ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(15))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                ))
                .build();
        this.objectMapper = objectMapper;
    }

    public Mono<String> generateSuiAddress() {
        return webClient.post()
                .uri(walletsEndpoint)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .doOnNext(body -> log.error("Wallet API error: {}", body))
                                .flatMap(body -> Mono.error(new ServiceUnavailableException(
                                        "Wallet service unavailable: " + body)))
                )
                .bodyToMono(String.class)
                .flatMap(this::parseResponse)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof WebClientRequestException ||
                                (ex instanceof WebClientResponseException &&
                                        ((WebClientResponseException) ex).getStatusCode().is5xxServerError())))
                .timeout(Duration.ofSeconds(15))
                .doOnSubscribe(sub -> log.info("Wallet address generation started"))
                .doOnSuccess(addr -> log.info("Generated address: {}", addr))
                .doOnError(e -> log.error("Critical failure", e));
    }

    private Mono<String> parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.has("error")) {
                return Mono.error(new ServiceUnavailableException(root.get("error").asText()));
            }
            JsonNode addressNode = root.path("data").path("address");
            if (addressNode.isMissingNode()) {
                addressNode = root.path("address");
            }
                if (!addressNode.isTextual()) {
                    log.error("Invalid address format. Response length: {}", rawResponse.length());
                    return Mono.error(new IllegalStateException("Invalid address format"));
                }
            String address = addressNode.asText();
            if (!address.startsWith("0x") || address.length() != 66) {
                        log.error("Invalid Sui address format: {}", address);
                return Mono.error(new IllegalStateException("Invalid Sui address: " + address));
            }
            return Mono.just(address);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response. Response length: {}", rawResponse.length());
            return Mono.error(new IllegalStateException("Invalid API response format", e));
        }
    }

}
