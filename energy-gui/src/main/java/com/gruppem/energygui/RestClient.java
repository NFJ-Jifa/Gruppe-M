package com.gruppem.energygui;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RestClient {

    public static String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getCurrentEnergyData() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/energy/current"))
                .GET()
                .build();
        var resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Unexpected status: " + resp.statusCode());
        }
        return resp.body();
    }

    public String getHistoricalEnergyData(String startTime, String endTime) throws IOException, InterruptedException {
        String url = String.format(
                BASE_URL + "/energy/historical?start=%s&end=%s",
                URLEncoder.encode(startTime, StandardCharsets.UTF_8),
                URLEncoder.encode(endTime,   StandardCharsets.UTF_8)
        );
        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        var resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // 204 No Content → трактуем как пустой массив
        if (resp.statusCode() == 204) {
            return "[]";
        }
        if (resp.statusCode() != 200) {
            throw new IOException("Unexpected status: " + resp.statusCode());
        }
        var body = resp.body();
        return (body == null || body.isBlank()) ? "[]" : body;
    }

    /**
     * Новый метод: возвращает JSON вида
     *   {"from":"2025-06-19T12:00:00Z","to":"2025-06-21T15:00:00Z"}
     */
    public String getAvailableRange() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/energy/available-range"))
                .GET()
                .build();
        var resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Unexpected status: " + resp.statusCode());
        }
        return resp.body();
    }
}
