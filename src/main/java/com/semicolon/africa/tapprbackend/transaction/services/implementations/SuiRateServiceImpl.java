package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.semicolon.africa.tapprbackend.transaction.data.models.ExchangeRate;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.ExchangeRateRepository;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.SuiRateService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
//@RequiredArgsConstructor
public class SuiRateServiceImpl implements SuiRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYMBOL = "SUI/NGN";
    private static final BigDecimal FALLBACK_RATE = new BigDecimal("345.00"); // default in case API fails

    @Value("${sui.price.api.url:https://api.coingecko.com/api/v3/simple/price?ids=sui&vs_currencies=ngn}")
    private String coingeckoUrl;

    public SuiRateServiceImpl(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @PostConstruct
    public void initRateIfMissing() {
        exchangeRateRepository.findBySymbol(SYMBOL).orElseGet(() -> {
            ExchangeRate rate = new ExchangeRate(null, SYMBOL, FALLBACK_RATE, LocalDateTime.now());
            return exchangeRateRepository.save(rate);
        });
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
        return ngnAmount.divide(getSuiToNgnRate(), 6, RoundingMode.HALF_UP);
    }

    /**
     * Scheduled to update every 5 minutes
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void updateExchangeRate() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(coingeckoUrl, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Map<String, Object>> body = response.getBody();
                Object rateObj = body.get("sui").get("ngn");

                if (rateObj != null) {
                    BigDecimal newRate = new BigDecimal(rateObj.toString());
                    ExchangeRate rate = exchangeRateRepository.findBySymbol(SYMBOL)
                            .orElse(new ExchangeRate(null, SYMBOL, newRate, LocalDateTime.now()));
                    rate.setRate(newRate);
                    rate.setLastUpdated(LocalDateTime.now());
                    exchangeRateRepository.save(rate);

                    log.info("✅ SUI↔NGN rate updated: 1 SUI = ₦{}", newRate);
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to update SUI↔NGN rate: {}", e.getMessage());
        }
    }
}
