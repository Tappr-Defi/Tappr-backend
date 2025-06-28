package com.semicolon.africa.tapprbackend.transaction.services.interfaces;

import java.math.BigDecimal;

public interface SuiRateService {
    BigDecimal getSuiToNgnRate();
    BigDecimal convertSuiToNgn(BigDecimal suiAmount);
    BigDecimal convertNgnToSui(BigDecimal ngnAmount);
}
