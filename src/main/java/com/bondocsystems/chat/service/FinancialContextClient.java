package com.bondocsystems.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Retrieves financial context from the system of record. The end-user's JWT is
 * forwarded so Transaction Service and Account Service enforce ownership.
 */
@Component
public class FinancialContextClient {
    private static final Logger log = LoggerFactory.getLogger(FinancialContextClient.class);
    private final RestClient restClient;

    public FinancialContextClient(RestClient.Builder builder,
            @Value("${fintech.transaction.base-url:http://localhost:8087}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String getContext(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("A customer access token is required to retrieve financial context");
        }
        try {
            String context = restClient.get().uri("/transactions/ai-context?days=90&limit=250")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve().body(String.class);
            return context == null ? "{}" : context;
        } catch (RuntimeException exception) {
            log.warn("Unable to retrieve customer financial context: {}", exception.getClass().getSimpleName());
            throw new IllegalStateException("Your financial data is temporarily unavailable. Please try again.", exception);
        }
    }
}
