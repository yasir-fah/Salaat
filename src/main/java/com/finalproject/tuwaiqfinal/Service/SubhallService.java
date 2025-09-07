package com.finalproject.tuwaiqfinal.Service;

import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOout.ReviewHallDTO;
import com.finalproject.tuwaiqfinal.DTOout.ReviewSubHallDTO;
import com.finalproject.tuwaiqfinal.Model.Hall;
import com.finalproject.tuwaiqfinal.Model.Owner;
import com.finalproject.tuwaiqfinal.Model.SubHall;
import com.finalproject.tuwaiqfinal.Repository.HallRepository;
import com.finalproject.tuwaiqfinal.Repository.OwnerRepository;
import com.finalproject.tuwaiqfinal.Repository.SubHallRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SubhallService {

    private final HallRepository hallRepository;
    private final SubHallRepository subHallRepository;
    private final OwnerRepository ownerRepository;

    //todo: review subhall service

    public SubHall getSingleSubhall(Integer subHallId){
        return subHallRepository.findById(subHallId)
                .orElseThrow(()-> new ApiException("sub hall not found"));
    }

    public void addSubHall(Integer ownerId,Integer hallId ,SubHall subHall){
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()-> new ApiException("owner not found"));

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(()-> new ApiException("hall not found"));

        if (!hall.getOwner().equals(owner))
            throw new ApiException("permission denied");

        subHall.setHall(hall);
        subHallRepository.save(subHall);
        log.info("subHall:{} added to hall:{} by owner:{}",subHall.getId(),hall.getId(),owner.getId());
    }

    public void updateSubHall(Integer ownerId,Integer subHallId,SubHall subHall){
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()->new ApiException("owner not found"));

        SubHall oldSubHall = subHallRepository.findById(subHallId)
                .orElseThrow(()-> new ApiException("sub hall not found"));

        if (!subHall.getHall().getOwner().equals(owner))
            throw new ApiException("permission denied");

        oldSubHall.setName(subHall.getName());
        oldSubHall.setDescription(subHall.getDescription());
        oldSubHall.setPricePerHour(subHall.getPricePerHour());

        if (subHall.getHall() == null || subHall.getHall().getId() == null)
            throw new ApiException("hall id is required");

        Hall hall = hallRepository.findById(subHall.getHall().getId())
                .orElseThrow(()-> new ApiException("hall not found"));
        oldSubHall.setHall(hall);
        subHallRepository.save(oldSubHall);
        log.info("subHall:{} updated",subHall.getId());
    }

    //cannot delete because cascade
    
    public void deleteSubhall(Integer ownerId, Integer subHallId){
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(()-> new ApiException("owner not found"));
        
        SubHall subHall = subHallRepository.findById(subHallId)
                .orElseThrow(()-> new ApiException("sub hall not found"));

        Hall hall = hallRepository.findById(subHall.getHall().getId())
                .orElseThrow(()-> new ApiException("hall not found"));
        
        if (!subHall.getHall().getOwner().getId().equals(owner.getId()))
            throw new ApiException("forbidden access");

        if (!subHall.getHall().equals(hall))
            throw new ApiException("subHall is not assigned with this main hall");

        subHall.setHall(null);
        subHallRepository.delete(subHall);
        log.info("subHall:{} deleted",subHall.getId());
    }

    public List<ReviewSubHallDTO> getSubHallsWithinBudget(Integer hallId, Integer subHallId, Integer pricePerHour) {


        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ApiException("hall not found"));


        SubHall subHall = subHallRepository.findSubHallsById(subHallId);
        if (subHall == null) {
            throw new ApiException("sub hall not found");
        }
        if (subHall.getHall() == null || !subHall.getHall().getId().equals(hall.getId()))
            throw new ApiException("sub hall does not belong to this hall");


        if (pricePerHour == null || pricePerHour < 0)
            throw new ApiException("invalid price");

        List<SubHall> rows = subHallRepository.findByHall_IdAndPricePerHourLessThanEqualOrderByPricePerHourAscIdAsc(hallId, pricePerHour);

        List<ReviewSubHallDTO> out = new java.util.ArrayList<>();
        for (SubHall s : rows) {
            out.add(new ReviewSubHallDTO(s.getId(), s.getName(), s.getPricePerHour(), s.getDescription()));
        }

        return out;
    }
}
