package com.finalproject.tuwaiqfinal.Controller;

import com.finalproject.tuwaiqfinal.Api.ApiResponse;
import com.finalproject.tuwaiqfinal.DTOout.ReviewHallDTO;
import com.finalproject.tuwaiqfinal.DTOout.ReviewSubHallDTO;
import com.finalproject.tuwaiqfinal.Model.SubHall;
import com.finalproject.tuwaiqfinal.Service.SubhallService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subhall")
@AllArgsConstructor
public class SubHallController {

    private final SubhallService subhallService;

    @GetMapping("/get/{subHallId}")
    public ResponseEntity<?> getSingleSubhall(@PathVariable Integer subHallId){
        return ResponseEntity.ok(subhallService.getSingleSubhall(subHallId));
    }

    @PostMapping("/add/{hallId}")
    public ResponseEntity<?> addSubHall(@RequestParam Integer ownerId,@PathVariable Integer hallId, @RequestBody @Valid SubHall subHall){
        subhallService.addSubHall(ownerId,hallId, subHall);
        return ResponseEntity.ok(new ApiResponse("SubHall has been added"));
    }

    @PutMapping("/update/{subHallId}")
    public ResponseEntity<?> updateSubHall(@RequestParam Integer ownerId,@PathVariable Integer subHallId, @RequestBody @Valid SubHall subHall){
        subhallService.updateSubHall(ownerId,subHallId, subHall);
        return ResponseEntity.ok(new ApiResponse("SubHall has been updated"));
    }

    @DeleteMapping("/delete/{ownerId}/{subHallId}")
    public ResponseEntity<?> deleteSubhall(@PathVariable Integer ownerId, @PathVariable Integer subHallId){
        subhallService.deleteSubhall(ownerId, subHallId);
        return ResponseEntity.ok(new ApiResponse("SubHall has been deleted"));
    }

    @GetMapping("/hall/{hallId}/subhall/{subHallId}/budget/{pricePerHour}")
    public ResponseEntity<?> getSubHallsWithinBudget(@PathVariable Integer hallId, @PathVariable Integer subHallId, @PathVariable Integer pricePerHour) {
        List<ReviewSubHallDTO> list = subhallService.getSubHallsWithinBudget(hallId, subHallId, pricePerHour);
        return ResponseEntity.ok(list);
    }
}

