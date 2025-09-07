package com.finalproject.tuwaiqfinal.Service;

import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.Model.Hall;
import com.finalproject.tuwaiqfinal.Model.Owner;
import com.finalproject.tuwaiqfinal.Model.SubHall;
import com.finalproject.tuwaiqfinal.Repository.HallRepository;
import com.finalproject.tuwaiqfinal.Repository.OwnerRepository;
import com.finalproject.tuwaiqfinal.Repository.SubHallRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final OwnerRepository ownerRepository;
    private final SubHallRepository subHallRepository;

    public List<Hall> getAllHalls(){
        return hallRepository.findAll();
    }

    public Hall getSingleHall(Integer hallId){
        return hallRepository.findById(hallId)
                .orElseThrow(()-> new ApiException("hall not found"));
    }

    public void addHall(Integer ownerId,Hall hall){

        //check owner
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()->new ApiException("owner not found"));

        hall.setOwner(owner);
        hallRepository.save(hall);
    }

    public void updateHall(Integer hallId,Integer ownerId, Hall hall){

        Hall oldHall = hallRepository.findById(hallId)
                .orElseThrow(()-> new ApiException("hall not found"));

        oldHall.setName(hall.getName());
        oldHall.setDescription(hall.getDescription());
        oldHall.setStatus(hall.getStatus());
        oldHall.setLocation(hall.getLocation());

        //check owner
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()-> new ApiException("owner not found"));

        if (!hall.getOwner().equals(owner))
            throw new ApiException("permission denied");

        oldHall.setOwner(owner);
        hallRepository.save(oldHall);
    }

    public void deleteHall(Integer ownerId,Integer hallId){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(()-> new ApiException("hall not found"));

        Owner owner = ownerRepository.findById(ownerId)
                        .orElseThrow(()-> new ApiException("owner not found"));

        if (!hall.getOwner().equals(owner))
            throw new ApiException("permission denied");

        hall.setOwner(null);
        hallRepository.delete(hall);
    }

    public List<SubHall> getAllSubHalls(Integer hallId){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(()-> new ApiException("hall not found"));
        return subHallRepository.getSubHallByHall(hall);
    }

}
