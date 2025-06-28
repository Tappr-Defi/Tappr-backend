package com.semicolon.africa.tapprbackend.Wallet.enums;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.semicolon.africa.tapprbackend.transaction.enums.WalletCurrency;

public class CurrencyResolver {

    public static WalletCurrency resolveCurrencyFromCountry(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) return WalletCurrency.USD;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(countryCode, "");
            String regionCode = phoneNumberUtil.getRegionCodeForNumber(parsedNumber);


            return switch (regionCode) {
                // Major African countries
                case "NG" -> WalletCurrency.NGN;  // Nigeria
                case "KE" -> WalletCurrency.KES;    // Kenya
                case "GH" -> WalletCurrency.GHS;    // Ghana
                case "ZA" -> WalletCurrency.ZAR;    // South Africa
                case "EG" -> WalletCurrency.EGP;    // Egypt
                case "DZ" -> WalletCurrency.DZD;    // Algeria
                case "MA" -> WalletCurrency.MAD;    // Morocco
                case "TN" -> WalletCurrency.TND;    // Tunisia
                case "UG" -> WalletCurrency.UGX;    // Uganda
                case "TZ" -> WalletCurrency.TZS;    // Tanzania
                case "ET" -> WalletCurrency.ETB;    // Ethiopia
                case "AO" -> WalletCurrency.AOA;    // Angola
                case "CM" -> WalletCurrency.XAF;    // Cameroon
                case "CI" -> WalletCurrency.XOF;    // Ivory Coast
                case "SN" -> WalletCurrency.XOF;    // Senegal
                case "ML" -> WalletCurrency.XOF;    // Mali
                case "NE" -> WalletCurrency.XOF;    // Niger
                case "BF" -> WalletCurrency.XOF;    // Burkina Faso
                case "TG" -> WalletCurrency.XOF;    // Togo
                case "BJ" -> WalletCurrency.XOF;    // Benin
                case "CF" -> WalletCurrency.XAF;    // Central African Republic
                case "GA" -> WalletCurrency.XAF;    // Gabon
                case "CG" -> WalletCurrency.XAF;    // Republic of the Congo
                case "CD" -> WalletCurrency.CDF;    // DR Congo
                case "MW" -> WalletCurrency.MWK;    // Malawi
                case "ZM" -> WalletCurrency.ZMW;    // Zambia
                case "ZW" -> WalletCurrency.ZWL;    // Zimbabwe
                case "RW" -> WalletCurrency.RWF;    // Rwanda
                case "LS" -> WalletCurrency.LSL;    // Lesotho
                case "NA" -> WalletCurrency.NAD;    // Namibia
                case "BW" -> WalletCurrency.BWP;    // Botswana
                case "MZ" -> WalletCurrency.MZN;    // Mozambique
                case "GM" -> WalletCurrency.GMD;    // Gambia
                case "SL" -> WalletCurrency.SLL;    // Sierra Leone
                case "LR" -> WalletCurrency.LRD;    // Liberia
                case "GN" -> WalletCurrency.GNF;    // Guinea
                case "GW" -> WalletCurrency.XOF;    // Guinea-Bissau
                case "MR" -> WalletCurrency.MRU;    // Mauritania
                case "SC" -> WalletCurrency.SCR;    // Seychelles
                case "CV" -> WalletCurrency.CVE;    // Cape Verde
                case "DJ" -> WalletCurrency.DJF;    // Djibouti
                case "ER" -> WalletCurrency.ERN;    // Eritrea
                case "KM" -> WalletCurrency.KMF;    // Comoros
                case "ST" -> WalletCurrency.STN;    // Sao Tome and Principe
                case "SZ" -> WalletCurrency.SZL;    // Eswatini
                case "SO" -> WalletCurrency.SOS;    // Somalia
                case "SS" -> WalletCurrency.SSP;    // South Sudan
                case "EH" -> WalletCurrency.MAD;    // Western Sahara (Moroccan Dirham)

                // Major global countries
                case "US" -> WalletCurrency.USD;    // United States
                case "GB" -> WalletCurrency.GBP;    // United Kingdom
                case "EU" -> WalletCurrency.EUR;    // European Union
                case "IN" -> WalletCurrency.INR;    // India
                case "JP" -> WalletCurrency.JPY;    // Japan
                case "CN" -> WalletCurrency.CNY;    // China
                case "AE" -> WalletCurrency.AED;    // UAE
                case "CA" -> WalletCurrency.CAD;    // Canada
                case "AU" -> WalletCurrency.AUD;    // Australia
                case "BR" -> WalletCurrency.BRL;    // Brazil

                default -> WalletCurrency.SUI; // fallback currency
            };
        } catch (NumberParseException e) {
            return WalletCurrency.SUI; // fallback currency
        }
    }
}
