package com.furniro.OrderService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.furniro.OrderService.config.PayPalConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class PayPalService {

    @Autowired
    private PayPalConfig config;

    private final String returnUrl = "http://localhost:3000/success?title=order&message=order%20successfully";
    private final String cancelUrl = "http://localhost:3000/error?title=order&message=payment%20failed";

    private final String BASE_URL = "https://api-m.sandbox.paypal.com";

    public String getAccessToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(config.getClientId(), config.getSecret());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_URL + "/v1/oauth2/token",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
            throw new RuntimeException("Failed to get PayPal access token: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("PayPal Authentication Error: " + e.getMessage());
        }
    }

    public Map<String, Object> createOrder(String amountValue, String currencyCode , Integer orderId) {
        try {
            String accessToken = getAccessToken();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("intent", "CAPTURE");

            if ("VND".equalsIgnoreCase(currencyCode)) {
                BigDecimal vndAmount = new BigDecimal(amountValue);
                BigDecimal exchangeRate = new BigDecimal("25000");
                BigDecimal usdAmount = vndAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
                amountValue = usdAmount.toPlainString();
                currencyCode = "USD";
            }

            // 1. Cấu hình Amount
            Map<String, String> amount = new HashMap<>();
            amount.put("currency_code", currencyCode);
            amount.put("value", amountValue);

            // 2. Cấu hình Purchase Unit
            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("amount", amount);
            body.put("purchase_units", List.of(purchaseUnit));

            // 3. THÊM MỚI: Cấu hình application_context để xử lý điều hướng quay về website
            Map<String, String> applicationContext = new HashMap<>();
            applicationContext.put("return_url", returnUrl + "&type=order&orderId=" + orderId); // Truyền orderId để sau này xác định đơn hàng nào đã thanh toán
            applicationContext.put("cancel_url", cancelUrl);
            applicationContext.put("landing_page", "LOGIN"); // Ép PayPal hiển thị trang Login trước
            applicationContext.put("user_action", "PAY_NOW"); // Hiển thị nút "Pay Now" thay vì "Continue" trên PayPal

            body.put("application_context", applicationContext);

            // Gửi request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_URL + "/v2/checkout/orders",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to create PayPal order: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("PayPal Order Creation Error: " + e.getMessage());
        }
    }

    public Map<String, Object> captureOrder(String orderId) {
        try {
            String accessToken = getAccessToken();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_URL + "/v2/checkout/orders/" + orderId + "/capture",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to capture PayPal order: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("PayPal Order Capture Error: " + e.getMessage());
        }
    }

    public Map<String, Object> getOrderDetails(String orderId) {
        try {
            String accessToken = getAccessToken();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE_URL + "/v2/checkout/orders/" + orderId,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to get PayPal order details: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("PayPal Order Fetch Error: " + e.getMessage());
        }
    }
}
