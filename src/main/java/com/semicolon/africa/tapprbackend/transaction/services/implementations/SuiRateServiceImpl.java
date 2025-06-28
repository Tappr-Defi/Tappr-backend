package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semicolon.africa.tapprbackend.transaction.data.models.ExchangeRate;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.ExchangeRateRepository;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.SuiRateService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuiRateServiceImpl implements SuiRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String SYMBOL = "SUI/NGN";
    private static final BigDecimal FALLBACK_RATE = new BigDecimal("345.00");
    private static final long UPDATE_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    @Value("${sui.price.api.url:https://api.coingecko.com/api/v3/simple/price?ids=sui&vs_currencies=ngn}")
    private String coingeckoUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void initRateIfMissing() {
        try {
            if (!exchangeRateRepository.existsBySymbol(SYMBOL)) {
                ExchangeRate rate = new ExchangeRate(null, SYMBOL, FALLBACK_RATE, LocalDateTime.now());
                exchangeRateRepository.save(rate);
                log.info("Initialized exchange rate with fallback value: {}", FALLBACK_RATE);
            }
        } catch (Exception e) {
            log.warn("Failed to initialize exchange rate, will use fallback rate when needed: {}", e.getMessage());
        }
    }

    @Override
    public BigDecimal getSuiToNgnRate() {
        return exchangeRateRepository.findBySymbol(SYMBOL)
                .map(ExchangeRate::getRate)
                .orElse(FALLBACK_RATE);
    }

    @Override
    public BigDecimal convertSuiToNgn(BigDecimal suiAmount) {
        return suiAmount.multiply(getSuiToNgnRate());
    }

    @Override
    public BigDecimal convertNgnToSui(BigDecimal ngnAmount) {
        BigDecimal rate = getSuiToNgnRate();
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot convert NGN to SUI with zero exchange rate");
        }
        return ngnAmount.divide(rate, 6, RoundingMode.HALF_UP);
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    public void updateExchangeRate() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(coingeckoUrl, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("API request failed. Status: {}", response.getStatusCode());
                return;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode suiNode = root.path("sui");

            if (suiNode.isMissingNode()) {
                log.warn("'sui' node not found in API response");
                return;
            }

            JsonNode ngnNode = suiNode.path("ngn");

            if (!ngnNode.isValueNode()) {
                log.warn("'ngn' value not found or invalid");
                return;
            }

            BigDecimal newRate = parseRateValue(ngnNode);
            updateRateInDatabase(newRate);

        } catch (Exception e) {
            log.warn("⚠️ Failed to update SUI↔NGN rate: {}", e.getMessage(), e);
        }
    }

    private BigDecimal parseRateValue(JsonNode ngnNode) {
        try {
            if (ngnNode.isNumber()) {
                return ngnNode.decimalValue();
            } else if (ngnNode.isTextual()) {
                return new BigDecimal(ngnNode.asText().replace(",", ""));
            } else {
                throw new IllegalArgumentException("Unsupported value type for rate");
            }
        } catch (Exception e) {
            log.warn("Rate parsing error. Using fallback rate. Error: {}", e.getMessage());
            return FALLBACK_RATE;
        }
    }

    private void updateRateInDatabase(BigDecimal newRate) {
        Optional<ExchangeRate> existingRate = exchangeRateRepository.findBySymbol(SYMBOL);
        ExchangeRate rate = existingRate.orElseGet(() ->
                new ExchangeRate(null, SYMBOL, newRate, LocalDateTime.now())
        );

        if (existingRate.isPresent() && newRate.compareTo(existingRate.get().getRate()) == 0) {
            log.debug("Rate unchanged ({}), skipping update", newRate);
            return;
        }

        rate.setRate(newRate);
        rate.setLastUpdated(LocalDateTime.now());
        exchangeRateRepository.save(rate);
        log.info("✅ SUI↔NGN rate updated: 1 SUI = ₦{}", newRate);
    }
}