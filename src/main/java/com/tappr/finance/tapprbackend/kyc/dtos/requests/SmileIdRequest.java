package com.tappr.finance.tapprbackend.kyc.dtos.requests;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class SmileIdRequest {
    private String source_sdk;
    private String source_sdk_version;
    private String file_name;
    private String timestamp;
    private String signature;
    private String partner_params;
    private String callback_url;
    private Map<String, String> id_info;
}