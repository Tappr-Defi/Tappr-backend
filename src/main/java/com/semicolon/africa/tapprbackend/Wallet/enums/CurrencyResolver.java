package com.semicolon.africa.tapprbackend.Wallet.enums;


import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;

public class CurrencyResolver {

    public static CurrencyType resolveCurrencyFromCountry(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) return CurrencyType.USD;

        switch (countryCode.toUpperCase()) {
            // Major African countries
            case "NG": return CurrencyType.NGN;  // Nigeria
            case "KE": return CurrencyType.KES;    // Kenya
            case "GH": return CurrencyType.GHS;    // Ghana
            case "ZA": return CurrencyType.ZAR;    // South Africa
            case "EG": return CurrencyType.EGP;    // Egypt
            case "DZ": return CurrencyType.DZD;    // Algeria
            case "MA": return CurrencyType.MAD;    // Morocco
            case "TN": return CurrencyType.TND;    // Tunisia
            case "UG": return CurrencyType.UGX;    // Uganda
            case "TZ": return CurrencyType.TZS;    // Tanzania
            case "ET": return CurrencyType.ETB;    // Ethiopia
            case "AO": return CurrencyType.AOA;    // Angola
            case "CM": return CurrencyType.XAF;    // Cameroon
            case "CI": return CurrencyType.XOF;    // Ivory Coast
            case "SN": return CurrencyType.XOF;    // Senegal
            case "ML": return CurrencyType.XOF;    // Mali
            case "NE": return CurrencyType.XOF;    // Niger
            case "BF": return CurrencyType.XOF;    // Burkina Faso
            case "TG": return CurrencyType.XOF;    // Togo
            case "BJ": return CurrencyType.XOF;    // Benin
            case "CF": return CurrencyType.XAF;    // Central African Republic
            case "GA": return CurrencyType.XAF;    // Gabon
            case "CG": return CurrencyType.XAF;    // Republic of the Congo
            case "CD": return CurrencyType.CDF;    // DR Congo
            case "MW": return CurrencyType.MWK;    // Malawi
            case "ZM": return CurrencyType.ZMW;    // Zambia
            case "ZW": return CurrencyType.ZWL;    // Zimbabwe
            case "RW": return CurrencyType.RWF;    // Rwanda
            case "LS": return CurrencyType.LSL;    // Lesotho
            case "NA": return CurrencyType.NAD;    // Namibia
            case "BW": return CurrencyType.BWP;    // Botswana
            case "MZ": return CurrencyType.MZN;    // Mozambique
            case "GM": return CurrencyType.GMD;    // Gambia
            case "SL": return CurrencyType.SLL;    // Sierra Leone
            case "LR": return CurrencyType.LRD;    // Liberia
            case "GN": return CurrencyType.GNF;    // Guinea
            case "GW": return CurrencyType.XOF;    // Guinea-Bissau
            case "MR": return CurrencyType.MRU;    // Mauritania
            case "SC": return CurrencyType.SCR;    // Seychelles
            case "CV": return CurrencyType.CVE;    // Cape Verde
            case "DJ": return CurrencyType.DJF;    // Djibouti
            case "ER": return CurrencyType.ERN;    // Eritrea
            case "KM": return CurrencyType.KMF;    // Comoros
            case "ST": return CurrencyType.STN;    // Sao Tome and Principe
            case "SZ": return CurrencyType.SZL;    // Eswatini
            case "SO": return CurrencyType.SOS;    // Somalia
            case "SS": return CurrencyType.SSP;    // South Sudan
            case "EH": return CurrencyType.MAD;    // Western Sahara (Moroccan Dirham)

            // Major global countries
            case "US": return CurrencyType.USD;    // United States
            case "GB": return CurrencyType.GBP;    // United Kingdom
            case "EU": return CurrencyType.EUR;    // European Union
            case "IN": return CurrencyType.INR;    // India
            case "JP": return CurrencyType.JPY;    // Japan
            case "CN": return CurrencyType.CNY;    // China
            case "AE": return CurrencyType.AED;    // UAE
            case "CA": return CurrencyType.CAD;    // Canada
            case "AU": return CurrencyType.AUD;    // Australia
            case "BR": return CurrencyType.BRL;    // Brazil

            default: return CurrencyType.NAIRA; // fallback currency
        }
    }
}
