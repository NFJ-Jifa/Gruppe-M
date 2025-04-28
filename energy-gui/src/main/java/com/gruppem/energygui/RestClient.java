package com.gruppem.energygui;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RestClient {

    private static final String BASE_URL = "http://localhost:8080";  // Адрес нашего API

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getCurrentEnergyData() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/energy/current"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getHistoricalEnergyData(String startTime, String endTime) throws IOException, InterruptedException {
        String url = String.format(
                BASE_URL + "/energy/historical?start=%s&end=%s",
                URLEncoder.encode(startTime, StandardCharsets.UTF_8),
                URLEncoder.encode(endTime, StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
