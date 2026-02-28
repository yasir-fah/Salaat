package com.finalproject.tuwaiqfinal.Controller;

import com.finalproject.tuwaiqfinal.Api.ApiResponse;
import com.finalproject.tuwaiqfinal.DTOin.OwnerDTO;
import com.finalproject.tuwaiqfinal.DTOout.ReviewFeedbackDTO;
import com.finalproject.tuwaiqfinal.Model.User;
import com.finalproject.tuwaiqfinal.Service.HallService;
import com.finalproject.tuwaiqfinal.Service.OwnerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner")
@AllArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllOwners(){
        return ResponseEntity.ok(ownerService.getAllOwners());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getSingleOwner(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ownerService.getSingleOwner(user.getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@RequestBody @Valid OwnerDTO ownerDTO){
        ownerService.registerOwner(ownerDTO);
        return ResponseEntity.ok(new ApiResponse("owner have been registered"));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateOwner(@AuthenticationPrincipal User user, @RequestBody @Valid OwnerDTO ownerDTO){
        ownerService.updateOwner(user.getId(),ownerDTO);
        return ResponseEntity.ok(new ApiResponse("owner have been updated"));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteOwner(@AuthenticationPrincipal User user){
        ownerService.deleteOwner(user.getId());
        return ResponseEntity.ok(new ApiResponse("owner have been deleted"));
    }

    @GetMapping("/feedback/{hall_id}")
    public ResponseEntity<?> reviewFeedback(@AuthenticationPrincipal User user,
                                            @PathVariable Integer hall_id) {
        ReviewFeedbackDTO result = ownerService.reviewFeedback(user.getId(),hall_id);
        return ResponseEntity.status(200).body(result);
    }

    @GetMapping("/feedback/subhall/{sub_hall_id}")
    public ResponseEntity<?> subHallFeedback(@AuthenticationPrincipal User user,
                                             @PathVariable Integer sub_hall_id) {
        String result = ownerService.subHallFeedback(user.getId(),sub_hall_id);
        return ResponseEntity.status(200).body(new ApiResponse(result));
    }

    @DeleteMapping("/cancel/booking/{bookingId}")
    public ResponseEntity<ApiResponse> cancelBookingByOwner(
            @AuthenticationPrincipal User user,
            @PathVariable Integer bookingId) {
        ownerService.ownerCancelBooking(user.getId(), bookingId);
        return ResponseEntity.ok(new ApiResponse("Booking has been cancelled successfully by owner."));
    }
}
