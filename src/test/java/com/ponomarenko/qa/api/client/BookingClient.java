package com.ponomarenko.qa.api.client;

import com.ponomarenko.qa.api.model.Booking;
import com.ponomarenko.qa.config.TestConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.http.ContentType.JSON;

public class BookingClient {
    public Response createBooking(Booking booking) {
        return request().body(booking).post("/booking");
    }

    public Response getBooking(int bookingId) {
        return request().pathParam("id", bookingId).get("/booking/{id}");
    }

    private RequestSpecification request() {
        Duration timeout = Duration.ofMillis(TestConfig.getPositiveLong("api.timeoutMs"));
        return given()
                .baseUri(TestConfig.get("api.baseUrl"))
                .contentType(JSON)
                .accept("application/json")
                .config(config()
                        .httpClient(httpClientConfig()
                                .connectTimeout(timeout)
                                .readTimeout(timeout)
                                .connectionRequestTimeout(timeout))
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()));
    }
}
