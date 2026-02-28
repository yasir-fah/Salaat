package com.finalproject.tuwaiqfinal.DTOin;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {


    // Fields that user will provide
    @NotNull(message = "Members cannot be null")
    @Min(value = 1, message = "At least 1 member is required")
    private Integer members;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Minimum duration is 30 minutes")
    private Integer duration_minutes;


    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startAt;

    // Note: endAt, status and totalPrice will be calculated by the service
    // - endAt will be calculated as: startAt + duration_minutes
    // - status will be set to "PENDING" by default
    // - totalPrice will be calculated based on subHall price, duration, etc.
    // - Time conflict validation will be performed to prevent overlapping bookings
}