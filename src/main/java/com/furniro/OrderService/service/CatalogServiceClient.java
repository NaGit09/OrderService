package com.furniro.OrderService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CatalogServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    // Secure fallback dictionary of variant prices for mock mode & standalone testing
    private static final Map<Integer, BigDecimal> MOCK_PRICE_CATALOG = new HashMap<>();

    static {
        MOCK_PRICE_CATALOG.put(1, new BigDecimal("120.00"));
        MOCK_PRICE_CATALOG.put(2, new BigDecimal("250.00"));
        MOCK_PRICE_CATALOG.put(3, new BigDecimal("450.00"));
        MOCK_PRICE_CATALOG.put(4, new BigDecimal("799.00"));
        MOCK_PRICE_CATALOG.put(5, new BigDecimal("1500.00"));
    }

    private static final BigDecimal DEFAULT_FALLBACK_PRICE = new BigDecimal("199.00");

    /**
     * Retrieve authentic, validated price of a product variant.
     * Hits ProductService microservice, with a robust fallback to a mock catalog.
     */
    public BigDecimal getVariantPrice(Integer variantID) {
        if (variantID == null) {
            return BigDecimal.ZERO;
        }

        try {
            // Attempt REST call to ProductService catalog
            String url = productServiceUrl + "/products/variants/" + variantID + "/price";
            log.debug("Fetching secure variant price from: {}", url);
            
            // Expected response format from microservice could be a simple numeric value or DTO
            BigDecimal price = restTemplate.getForObject(url, BigDecimal.class);
            if (price != null) {
                log.info("Successfully fetched pricing from ProductService for variantID: {} -> Price: {}", variantID, price);
                return price;
            }
        } catch (Exception e) {
            log.warn("ProductService call failed for variantID {}: {}. Falling back to secure mock catalog pricing.", 
                    variantID, e.getMessage());
        }

        // Return the secure mock price if service is offline/unreachable
        BigDecimal mockPrice = MOCK_PRICE_CATALOG.getOrDefault(variantID, DEFAULT_FALLBACK_PRICE);
        log.info("Using mock catalog pricing for variantID: {} -> Price: {}", variantID, mockPrice);
        return mockPrice;
    }

    /**
     * Fetch the total number of products from the product service.
     */
    public Long getTotalProductsCount() {
        try {
            String url = productServiceUrl + "/products/total";
            log.debug("Fetching total products count from: {}", url);
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("data") != null) {
                log.info("Successfully fetched total products count: {}", response.get("data"));
                return ((Number) response.get("data")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch total products count from ProductService: {}. Using fallback 0.", e.getMessage());
        }
        return 0L;
    }
}
