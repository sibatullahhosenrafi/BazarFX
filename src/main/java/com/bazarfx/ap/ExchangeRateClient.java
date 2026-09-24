package com.bazarfx.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Week 7 - JSON Parsing and API Response Handling with Java.
 *
 * Calls the free, keyless open.er-api.com REST endpoint to get the current
 * BDT -> USD exchange rate, reads the raw JSON HTTP response body, and
 * converts it into a plain Java object ({@link ExchangeRateResponse}) with
 * Gson - covering the three things this week's syllabus calls out in one
 * small class: JSON structure, a parsing technique, and API response
 * handling.
 *
 * The HTTP call runs on {@link HttpClient}'s own async executor thread, not
 * the JavaFX Application Thread, so a slow or unreachable network never
 * freezes the UI (ties back into Week 4's concurrency material). Results
 * are cached briefly since the rate barely moves minute to minute and every
 * product-detail screen would otherwise trigger its own network call.
 */
public class ExchangeRateClient {

    private static final String API_URL = "https://open.er-api.com/v6/latest/BDT";
    private static final long CACHE_LIFETIME_MS = 10 * 60 * 1000; // 10 minutes
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private volatile double cachedUsdRate = 0.0;
    private volatile long cachedAtMillis = 0L;

    /**
     * Reports the current BDT -> USD rate to {@code onResult}, which is
     * invoked on whichever background thread the async HTTP call completes
     * on - callers touching JavaFX nodes must wrap their handler in
     * {@code Platform.runLater}. Reports {@code 0.0} on any failure (no
     * network, bad JSON, API error) so the UI can simply hide the
     * conversion instead of crashing.
     */
    public void fetchUsdRateAsync(Consumer<Double> onResult) {
        if (isCacheFresh()) {
            onResult.accept(cachedUsdRate);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseUsdRate)
                .exceptionally(ex -> {
                    System.err.println("Exchange rate lookup failed: " + ex.getMessage());
                    return 0.0;
                })
                .thenAccept(rate -> {
                    if (rate > 0) {
                        cachedUsdRate = rate;
                        cachedAtMillis = System.currentTimeMillis();
                    }
                    onResult.accept(rate);
                });
    }

    private boolean isCacheFresh() {
        return cachedUsdRate > 0 && (System.currentTimeMillis() - cachedAtMillis) < CACHE_LIFETIME_MS;
    }

    /** Turns the raw JSON response body into an {@link ExchangeRateResponse} and reads the USD rate out of it. */
    private double parseUsdRate(String jsonBody) {
        try {
            ExchangeRateResponse response = GSON.fromJson(jsonBody, ExchangeRateResponse.class);
            if (response == null || !response.isSuccess() || response.getRates() == null) {
                return 0.0;
            }
            return response.getRates().getOrDefault("USD", 0.0);
        } catch (JsonSyntaxException e) {
            System.err.println("Could not parse exchange rate JSON: " + e.getMessage());
            return 0.0;
        }
    }
}
