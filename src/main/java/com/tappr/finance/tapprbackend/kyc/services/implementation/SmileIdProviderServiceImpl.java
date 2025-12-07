package com.tappr.finance.tapprbackend.kyc.services.implementation;

import com.tappr.finance.tapprbackend.kyc.config.SmileIdConfig;
import com.tappr.finance.tapprbackend.kyc.dtos.requests.IdVerificationRequest;
import com.tappr.finance.tapprbackend.kyc.dtos.responses.KycProviderResponse;
import com.tappr.finance.tapprbackend.kyc.enums.KycStatus;
import com.tappr.finance.tapprbackend.kyc.services.interfaces.KycProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmileIdProviderServiceImpl implements KycProviderService {

    private final SmileIdConfig smileIdConfig;
    private final RestTemplate restTemplate;

    @Override
    public KycProviderResponse submitVerification(UUID userId, IdVerificationRequest request) {
        try {
            String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            String signature = generateSignature(timestamp);

            Map<String, Object> payload = new HashMap<>();
            payload.put("partner_id", smileIdConfig.getPartnerId());
            payload.put("timestamp", timestamp);
            payload.put("signature", signature);
            payload.put("partner_params", generatePartnerParams(userId));
            payload.put("callback_url", smileIdConfig.getCallbackUrl());
            payload.put("source_sdk", "rest_api");
            payload.put("source_sdk_version", "1.0.0");

            Map<String, String> idInfo = new HashMap<>();
            idInfo.put("country", "NG"); // Dynamic?
            idInfo.put("id_type", mapIdType(request.getIdType()));
            idInfo.put("id_number", request.getIdNumber()); // e.g., "00000000000"
            payload.put("id_info", idInfo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            log.info("Sending KYC request to: {}/verify_id", smileIdConfig.getUrl());

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    smileIdConfig.getUrl() + "/verify_id",
                    entity,
                    Map.class
            );

            return parseProviderResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("Network error calling Smile ID Sandbox", e);
            return buildErrorResponse("Network/API Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Internal error during KYC request", e);
            return buildErrorResponse("Internal Error: " + e.getMessage());
        }
    }

    private Map<String, Object> generatePartnerParams(UUID userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId.toString());
        params.put("job_id", UUID.randomUUID().toString());
        params.put("job_type", 5);
        return params;
    }

    private String generateSignature(String timestamp) throws Exception {
        // Concatenation rules for Signature: timestamp + partner_id + "sid_request"
        String data = timestamp + smileIdConfig.getPartnerId() + "sid_request";

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(
                smileIdConfig.getApiKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        sha256_HMAC.init(secret_key);

        return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private KycProviderResponse parseProviderResponse(Map<String, Object> responseMap) {
        KycProviderResponse kycResponse = new KycProviderResponse();

        if (responseMap == null) return buildErrorResponse("Empty response from provider");

        // Sandbox Result Code 1012 = Verified
        String resultCode = String.valueOf(responseMap.get("result_code"));
        String resultText = (String) responseMap.get("result_text");

        if ("1012".equals(resultCode)) {
            kycResponse.setSuccess(true);
            kycResponse.setStatus(KycStatus.VERIFIED);
            kycResponse.setMessage("Sandbox Verification Successful");

            } else {
            kycResponse.setSuccess(false);
            kycResponse.setStatus(KycStatus.FAILED);
            kycResponse.setMessage("Sandbox Failed: " + resultText + " (Code: " + resultCode + ")");
        }
        return kycResponse;
    }

    private KycProviderResponse buildErrorResponse(String message) {
        KycProviderResponse r = new KycProviderResponse();
        r.setSuccess(false);
        r.setStatus(KycStatus.FAILED);
        r.setMessage(message);
        return r;
    }

    private String mapIdType(String inputType) {
        if (inputType == null) return "BANK_VERIFICATION_NUMBER";
        if (inputType.equalsIgnoreCase("BVN")) return "BANK_VERIFICATION_NUMBER";
        if (inputType.equalsIgnoreCase("NIN")) return "NIN";
        return inputType;
    }
}