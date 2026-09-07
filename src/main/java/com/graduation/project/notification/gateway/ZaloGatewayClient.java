package com.graduation.project.notification.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.notification.config.ZaloReminderProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ZaloGatewayClient {

  private static final Pattern PHONE_PATTERN = Pattern.compile("^(0\\d{9}|\\+84\\d{9})$");

  private static final int MAX_TEXT_LENGTH = 1_000;

  private final ZaloReminderProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public ZaloGatewayClient(ZaloReminderProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(properties.httpTimeout()).build();
  }

  public void sendText(String phone, String text) {
    validate(phone, text);

    URI endpoint = properties.gatewayBaseUrl().resolve("/send-text-to-user");

    HttpRequest request =
        HttpRequest.newBuilder(endpoint)
            .timeout(properties.httpTimeout())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(phone, text)))
            .build();

    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      validateResponse(response);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();

      throw new ZaloGatewayException(0, "Zalo gateway request was interrupted", exception);
    } catch (IOException exception) {
      throw new ZaloGatewayException(0, "Cannot connect to Zalo gateway", exception);
    }
  }

  private String createRequestBody(String phone, String text) {
    try {
      return objectMapper.writeValueAsString(new SendTextRequest(phone, text));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Cannot serialize Zalo message", exception);
    }
  }

  private void validate(String phone, String text) {
    if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
      throw new IllegalArgumentException("Invalid Vietnamese phone number");
    }

    if (text == null || text.isBlank() || text.length() > MAX_TEXT_LENGTH) {
      throw new IllegalArgumentException("Message must contain between 1 and 1000 characters");
    }
  }

  private void validateResponse(HttpResponse<String> response) {
    int statusCode = response.statusCode();

    if (statusCode >= 200 && statusCode < 300) {
      return;
    }

    throw new ZaloGatewayException(statusCode, "Zalo gateway returned HTTP " + statusCode);
  }

  private record SendTextRequest(String phone, String text) {}
}
