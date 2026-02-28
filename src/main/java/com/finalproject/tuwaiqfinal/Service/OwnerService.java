package com.finalproject.tuwaiqfinal.Service;


import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOin.OwnerDTO;
import com.finalproject.tuwaiqfinal.DTOout.ReviewFeedbackDTO;
import com.finalproject.tuwaiqfinal.Model.*;
import com.finalproject.tuwaiqfinal.Repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final AuthRepository authRepository;
    private final ReviewHallRepository reviewHallRepository;
    private final HallRepository hallRepository;
    private final AiService aiService;
    private final BookingRepository bookingRepository;
    private final SubHallRepository subHallRepository;
    private final ReviewSubHallRepository reviewSubHallRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public List<Owner> getAllOwners(){
        return ownerRepository.findAll();
    }

    public Owner getSingleOwner(Integer ownerId){
        return ownerRepository.findById(ownerId)
                .orElseThrow(()-> new ApiException("owner not found"));
    }

    public void registerOwner(OwnerDTO ownerDTO){
        User user = new User();

        user.setUsername(ownerDTO.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(ownerDTO.getPassword()));
        user.setEmail(ownerDTO.getEmail());
        user.setRole("OWNER");
        authRepository.save(user);


        Owner owner = new Owner();
        owner.setAccount_serial_num(ownerDTO.getAccount_serial_num());
        owner.setUser(user);
        ownerRepository.save(owner);
    }

    public void updateOwner(Integer ownerId, OwnerDTO ownerDTO){
        Owner oldOwner= ownerRepository.findById(ownerId)
                .orElseThrow(()->new ApiException("owner not found"));

        User user = oldOwner.getUser();
        user.setUsername(ownerDTO.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(ownerDTO.getPassword()));
        user.setEmail(ownerDTO.getEmail());
        user.setRole("OWNER");
        authRepository.save(user);

        oldOwner.setAccount_serial_num(ownerDTO.getAccount_serial_num());
        oldOwner.setUser(user);
        ownerRepository.save(oldOwner);
    }

    public void deleteOwner(Integer ownerId){
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()-> new ApiException("owner not found"));

        ownerRepository.delete(owner);
    }

    public ReviewFeedbackDTO reviewFeedback(Integer ownerId, Integer hallId) {

        // check if owner exists
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if(owner == null) {
            throw new ApiException("owner not found");
        }

        // check if hall exists
        Hall hall = hallRepository.findHallById(hallId);
        if(hall == null) {
            throw new ApiException("hall not found");
        }

        // check if hall and owner belong to each other
        if(hall.getOwner() == null || !hall.getOwner().getId().equals(owner.getId())) {
            throw new ApiException("hall and owner not belong to each other");
        }

        // find all hall reviews from owner
        List<ReviewHall> reviewHalls = reviewHallRepository.findAllByHallId(hall.getId());

        if(reviewHalls.isEmpty()) {
            throw new ApiException("reviews are empty nothing to review");
        }
        return aiService.hallFeedback(reviewHalls);
    }

    // allow owner to cancel a booking
    public void ownerCancelBooking(Integer ownerId, Integer bookingId) {

        Owner owner = ownerRepository.findOwnerById(ownerId);
        if(owner == null) {
            throw new ApiException("owner not found");
        }

        Booking booking = bookingRepository.findBookingsById(bookingId);
        if(booking == null) {
            throw new ApiException("booking not found");
        }

        // check if booking's sub hall owned by owner
        if(!booking.getSubHall().getHall().getOwner().getId().equals(owner.getId())) {
            throw new ApiException("forbidden: this owner does not have premonition to delete booking with id:" +booking.getId());
        }

        // Align with cancellation rules used elsewhere
        if ("approved".equalsIgnoreCase(booking.getStatus())) {
            throw new ApiException("booking approved, can't cancel this booking");
        }
        if ("cancelled".equalsIgnoreCase(booking.getStatus())) {
            throw new ApiException("booking already cancelled");
        }


        // delete booking form game:
        bookingRepository.delete(booking);
    }


    // ai feedback for sub hall
    public String subHallFeedback(Integer ownerId,Integer subHallId) {

        // check if owner exists
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if(owner == null) {
            throw new ApiException("owner not found");
        }

        // check if subHall exist
        SubHall subHall = subHallRepository.findSubHallsById(subHallId);
        if(subHall == null) {
            throw new ApiException("sub hall not found");
        }

        // check if owner own this sub hall
        if(!subHall.getHall().getOwner().getId().equals(owner.getId())) {
            throw new ApiException("forbidden: owner does not own this sub hall");
        }

        // grab all reviews of sub hall
        List<ReviewSubHall> reviewSubHalls = reviewSubHallRepository.findAllBySubHall(subHall);

        if(reviewSubHalls.isEmpty()) {
            throw new ApiException("sorry, empty list reviews");
        }
        return aiService.subHallFeedback(reviewSubHalls);
    }
}
