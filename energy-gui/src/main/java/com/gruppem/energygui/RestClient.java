package com.gruppem.energygui;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Simple REST client for communicating with the Energy REST API.
 * Provides methods for retrieving current, historical, and available-range data.
 */
public class RestClient {

    // Base URL of the REST API (can be adjusted if running on another host/port)
    public static String BASE_URL = "http://localhost:8080";

    // Java built-in HTTP client (available from Java 11+)
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sends a GET request to the /energy/current endpoint.
     * Retrieves the latest energy status as a JSON string.
     *
     * @return JSON string representing current energy data
     * @throws IOException if network or server error occurs
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Sends a GET request to /energy/historical with time range parameters.
     * Automatically URL-encodes the start and end timestamps.
     *
     * @param startTime ISO timestamp for the beginning of the range
     * @param endTime   ISO timestamp for the end of the range
     * @return JSON array string (empty array if no data)
     * @throws IOException if request fails or server returns error
     * @throws InterruptedException if the request is interrupted
     */
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

        if (resp.statusCode() == 204) {   // No content available
            return "[]";
        }
        if (resp.statusCode() != 200) {
            throw new IOException("Unexpected status: " + resp.statusCode());
        }

        var body = resp.body();
        return (body == null || body.isBlank()) ? "[]" : body;
    }

    /**
     * Sends a GET request to the /energy/available-range endpoint.
     * Retrieves the minimum and maximum available timestamps.
     *
     * @return JSON object string like {"from":"...","to":"..."}
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
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
