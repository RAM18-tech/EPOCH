package com.dtv.ct.commercetool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
public class FeatureFlagProcessor {
    private String proxyHost = "pxyapp.proxy.att.com";

    private int port = 8080;
    @Autowired
    private RestTemplate restTemplateProxy;

    @PostConstruct
    private void proxyConfiguration() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        final HttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setProxy(new HttpHost(proxyHost, port, "http"))
                .build();
        requestFactory.setHttpClient(httpClient);
        restTemplateProxy.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
    }

    @Autowired
    private RestTemplate restTemplate;

    public String generateIXPFile() throws Exception {
        Map<String, String> responseMap = invokeAsync();
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        responseMap.entrySet().forEach(entry -> {
            Map<String, String> innerMap = new HashMap<>();
            String key = entry.getKey();
            String result = entry.getValue();
            System.out.println("Key: " + key + ", Result: " + result);
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            JsonObject jsonObject = element.getAsJsonObject().get("allocations").getAsJsonObject();
            for (String k : jsonObject.keySet()) {
                JsonObject obj = jsonObject.getAsJsonObject(k);
                String value = obj.get("value").getAsString();
                String expiration = obj.get("expiration").getAsString();
                innerMap.put(k, value);
                System.out.println("Key: " + k + ", Value: " + value + ", Expiration: " + expiration);
            }
            resultMap.put(key, innerMap);
            try {
            } catch (Exception e) {
                System.err.println("Error parsing JSON for key: " + key + ", Error: " + e.getMessage());
            }
        });

        System.out.println("Final Result Map: " + resultMap);
        String fileName = writeResultMapToExcel(resultMap, "feature_flags.xlsx");
        return fileName;
    }

    private static Map<String, String> readFeatureFlagUrls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = FeatureFlagProcessor.class.getClassLoader()
                .getResourceAsStream("featureFlagUrl.json")) {
            return mapper.readValue(is, Map.class);
        }
    }

    private Map<String, String> invokeAsync() throws Exception {
        Map<String, String> urlMap = readFeatureFlagUrls();
        Map<String, String> responseMap = new HashMap<>();
        CompletableFuture[] futures = urlMap.entrySet().stream()
                .map(entry -> getResponseAsync(entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        for (CompletableFuture<Map<String, String>> future : futures) {
            try {
                Map<String, String> result = future.get();
                responseMap.putAll(result);
            } catch (CompletionException e) {
                System.err.println("Error processing feature flag: " + e.getCause().getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }

        System.out.println("All feature flags processed. Response map: " + responseMap);
        return responseMap;
    }

    private ResponseEntity<String> invokeEndpoint(String key, String url, HttpMethod method) throws RestClientException {
        try {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.set("Content-Type", "application/json");
            HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
            System.out.println("Invoking IXP endpoint: " + url + " with method: " + method);
            if (key.equalsIgnoreCase("production")) {
                ResponseEntity<String> result = restTemplateProxy.exchange(url, method, httpEntity, String.class);
                System.out.println("Response from IXP endpoint production: " + result.getBody());
                return result;
            }

            ResponseEntity<String> result1 = restTemplate.exchange(url, method, httpEntity, String.class);
            System.out.println("Response from IXP endpoint: " + result1.getBody());
            return result1;
        } catch (Exception e) {
            System.out.println("Error invoking IXP endpoint: " + url + " with method: " + method);
            throw new RestClientException("Error invoking IXP endpoint: " + e.getMessage(), e);
        }
    }


    private CompletableFuture<Map<String, String>> getResponseAsync(String key, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<String> response = invokeEndpoint(key, url, HttpMethod.GET);
                return Map.of(key, response.getBody());
            } catch (RestClientException e) {
                throw new CompletionException(e);
            }
        });

    }

    private static String writeResultMapToExcel(Map<String, Map<String, String>> resultMap, String filePath) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("FeatureFlags");

        // Collect all unique inner keys for the first column
        Set<String> allInnerKeys = new LinkedHashSet<>();
        for (Map<String, String> innerMap : resultMap.values()) {
            allInnerKeys.addAll(innerMap.keySet());
        }

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("FeatureFlag");
        int col = 1;
        for (String mainKey : resultMap.keySet()) {
            headerRow.createCell(col++).setCellValue(mainKey);
        }

        // Fill data rows
        int rowIdx = 1;
        for (String innerKey : allInnerKeys) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(innerKey);
            col = 1;
            for (String mainKey : resultMap.keySet()) {
                String value = resultMap.get(mainKey).get(innerKey);
                row.createCell(col++).setCellValue(value != null ? value : "false");
            }
        }

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();

        return sheet.getSheetName();
    }
}
