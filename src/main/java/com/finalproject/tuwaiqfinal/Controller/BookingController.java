package com.finalproject.tuwaiqfinal.Controller;

import com.finalproject.tuwaiqfinal.Api.ApiResponse;
import com.finalproject.tuwaiqfinal.DTOin.BookingDTO;
import com.finalproject.tuwaiqfinal.Model.Booking;
import com.finalproject.tuwaiqfinal.Model.User;
import com.finalproject.tuwaiqfinal.Service.GameService;
import com.finalproject.tuwaiqfinal.Service.bookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final bookingService bookingService;

    @GetMapping("/get")
    public ResponseEntity<?> getBookingByCustomer(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(200).body(bookingService.getBookingByCustomer(user.getId()));
    }

    @PostMapping("/add/booking/subhall/{subhall_id}")
    public ResponseEntity<?> addBooking(@AuthenticationPrincipal User user,
                                        @PathVariable Integer subhall_id,
                                        @Valid @RequestBody BookingDTO bookingDTO) {
        bookingService.addBooking(user.getId(), subhall_id, bookingDTO);
        return ResponseEntity.status(200).body(new ApiResponse("booking added successfully"));
    }

    @PutMapping("/update/booking/{booking_id}")
    public ResponseEntity<?> updateBooking(@AuthenticationPrincipal User user,
                                           @PathVariable Integer booking_id,
                                           @Valid @RequestBody BookingDTO bookingDTO) {
        bookingService.updateBooking(user.getId(), booking_id, bookingDTO);
        return ResponseEntity.status(200).body(new ApiResponse("booking updated successfully"));
    }

    @DeleteMapping("/delete/booking/{booking_id}")
    public ResponseEntity<?> deleteBooking(@AuthenticationPrincipal User user,
                                           @PathVariable Integer booking_id) {
        bookingService.deleteBooking(user.getId(), booking_id);
        return ResponseEntity.status(200).body(new ApiResponse("booking deleted successfully"));
    }

    @GetMapping("/initiated/hall/{hallId}")
    public ResponseEntity<?> getInitiatedByHall(@AuthenticationPrincipal User owner, @PathVariable Integer hallId) {
        List<Booking> bookings = bookingService.getInitiatedByHall(owner.getId(), hallId);
        return ResponseEntity.status(200).body(bookings);
    }

    @GetMapping("/Approved/hall/{hallId}")
    public ResponseEntity<?> getApprovedByHall(@AuthenticationPrincipal User owner, @PathVariable Integer hallId) {
        List<Booking> bookings = bookingService.getApprovedByHall(owner.getId(), hallId);
        return ResponseEntity.status(200).body(bookings);
    }

    @PostMapping("/remind-unpaid/{hallId}")
    public ResponseEntity<?> remindUnpaid(@AuthenticationPrincipal User owner, @PathVariable Integer hallId) {
        bookingService.remindUnpaidByHall(owner.getId(), hallId);
        return ResponseEntity.status(200).body(new ApiResponse("message is send successfully"));
    }

}