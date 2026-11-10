package com.ponomarenko.qa.api;

import com.ponomarenko.qa.api.client.BookingClient;
import com.ponomarenko.qa.api.model.Booking;
import com.ponomarenko.qa.api.model.BookingDates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.TEXT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

class BookingApiTest {
    @ParameterizedTest(name = "depositPaid={0}")
    @ValueSource(booleans = {true, false})
    @DisplayName("A created booking can be retrieved with the same data")
    void createdBookingCanBeRetrievedWithTheSameData(boolean depositPaid) {
        var booking = newBooking(depositPaid);
        var dates = booking.bookingDates();
        var client = new BookingClient();
        int bookingId = createBooking(client, booking);

        client.getBooking(bookingId)
                .then()
                .onFailMessage("GET /booking/" + bookingId + " should match the submitted booking")
                .statusCode(200)
                .contentType(JSON)
                .body(
                        "firstname", equalTo(booking.firstName()),
                        "lastname", equalTo(booking.lastName()),
                        "totalprice", equalTo(booking.totalPrice()),
                        "depositpaid", equalTo(booking.depositPaid()),
                        "bookingdates.checkin", equalTo(dates.checkIn()),
                        "bookingdates.checkout", equalTo(dates.checkOut()),
                        "additionalneeds", equalTo(booking.additionalNeeds())
                );
    }

    @Test
    @DisplayName("A missing booking returns 404")
    void missingBookingReturnsNotFound() {
        // The service assigns positive IDs, so zero cannot identify a booking.
        int missingBookingId = 0;

        new BookingClient().getBooking(missingBookingId)
                .then()
                .onFailMessage("GET /booking/0 should report a missing booking")
                .statusCode(404)
                .contentType(TEXT)
                .body(equalTo("Not Found"));
    }

    @Test
    @DisplayName("Deleting a booking without authentication is forbidden and preserves it")
    void deletingBookingWithoutAuthenticationIsForbidden() {
        var client = new BookingClient();
        var booking = newBooking(true);
        int bookingId = createBooking(client, booking);

        client.deleteBooking(bookingId)
                .then()
                .onFailMessage("DELETE /booking/" + bookingId + " without authentication should be forbidden")
                .statusCode(403)
                .contentType(TEXT)
                .body(equalTo("Forbidden"));

        client.getBooking(bookingId)
                .then()
                .onFailMessage("Booking " + bookingId + " should still exist after the forbidden DELETE")
                .statusCode(200)
                .contentType(JSON)
                .body("firstname", equalTo(booking.firstName()));
    }

    private static int createBooking(BookingClient client, Booking booking) {
        return client.createBooking(booking)
                .then()
                .onFailMessage("POST /booking should create a booking for " + booking.firstName())
                .statusCode(200)
                .contentType(JSON)
                .body("bookingid", greaterThan(0))
                .extract().path("bookingid");
    }

    private static Booking newBooking(boolean depositPaid) {
        LocalDate checkIn = LocalDate.now(ZoneOffset.UTC).plusDays(7);
        return new Booking(
                "QA" + UUID.randomUUID().toString().substring(0, 8),
                "Automation",
                321,
                depositPaid,
                new BookingDates(checkIn.toString(), checkIn.plusDays(3).toString()),
                "Breakfast"
        );
    }
}
