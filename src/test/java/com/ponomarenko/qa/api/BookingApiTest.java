package com.ponomarenko.qa.api;

import com.ponomarenko.qa.api.client.BookingClient;
import com.ponomarenko.qa.api.model.Booking;
import com.ponomarenko.qa.api.model.BookingDates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

class BookingApiTest {
    @ParameterizedTest(name = "depositPaid={0}")
    @ValueSource(booleans = {true, false})
    @DisplayName("A created booking can be retrieved with the same data")
    void createdBookingCanBeRetrievedWithTheSameData(boolean depositPaid) {
        LocalDate checkIn = LocalDate.now(ZoneOffset.UTC).plusDays(7);
        var dates = new BookingDates(checkIn.toString(), checkIn.plusDays(3).toString());
        var booking = new Booking(
                "QA" + UUID.randomUUID().toString().substring(0, 8),
                "Automation",
                321,
                depositPaid,
                dates,
                "Breakfast"
        );
        var client = new BookingClient();

        int bookingId = client.createBooking(booking)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("bookingid", greaterThan(0))
                .extract().path("bookingid");

        client.getBooking(bookingId)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("firstname", equalTo(booking.firstName()))
                .body("lastname", equalTo(booking.lastName()))
                .body("totalprice", equalTo(booking.totalPrice()))
                .body("depositpaid", equalTo(booking.depositPaid()))
                .body("bookingdates.checkin", equalTo(dates.checkIn()))
                .body("bookingdates.checkout", equalTo(dates.checkOut()))
                .body("additionalneeds", equalTo(booking.additionalNeeds()));
    }
}
