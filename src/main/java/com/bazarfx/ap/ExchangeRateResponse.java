package com.bazarfx.api;

import java.util.Map;

/**
 * Week 7 - JSON Parsing and API Response Handling with Java.
 *
 * Plain Java object that mirrors the JSON returned by
 * https://open.er-api.com/v6/latest/{BASE_CURRENCY}, e.g.:
 * <pre>
 * {
 *   "result": "success",
 *   "base_code": "BDT",
 *   "time_last_update_utc": "Mon, 21 Sep 2026 00:00:03 +0000",
 *   "rates": { "USD": 0.0084, "EUR": 0.0077, ... }
 * }
 * </pre>
 * Field names match the JSON keys exactly, which is what lets Gson
 * deserialize the whole response straight into this object with no manual
 * string parsing anywhere in the app.
 */
public class ExchangeRateResponse {

    private String result;
    private String base_code;
    private String time_last_update_utc;
    private Map<String, Double> rates;

    public String getResult() {
        return result;
    }

    public String getBaseCode() {
        return base_code;
    }

    public String getTimeLastUpdateUtc() {
        return time_last_update_utc;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(result);
    }
}
