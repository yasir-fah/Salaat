package com.finalproject.tuwaiqfinal.Controller;

import com.finalproject.tuwaiqfinal.Api.ApiResponse;
import com.finalproject.tuwaiqfinal.Model.Hall;
import com.finalproject.tuwaiqfinal.Service.HallService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hall")
@AllArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllHalls(){
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/get/{hallId}")
    public ResponseEntity<?> getSingleHall(@PathVariable Integer hallId){
        return ResponseEntity.ok(hallService.getSingleHall(hallId));
    }

    @PostMapping("/add/{ownerId}")
    public ResponseEntity<?> addHall(@PathVariable Integer ownerId, @RequestBody @Valid Hall hall){
        hallService.addHall(ownerId, hall);
        return ResponseEntity.ok(new ApiResponse("Hall has been added"));
    }

    @PutMapping("/update/{hallId}/{ownerId}")
    public ResponseEntity<?> updateHall(@PathVariable Integer hallId, @PathVariable Integer ownerId, @RequestBody @Valid Hall hall){
        hallService.updateHall(hallId, ownerId, hall);
        return ResponseEntity.ok(new ApiResponse("Hall has been updated"));
    }

    @DeleteMapping("/delete/{hallId}")
    public ResponseEntity<?> deleteHall(@RequestParam Integer ownerId,@PathVariable Integer hallId){
        hallService.deleteHall(ownerId,hallId);
        return ResponseEntity.ok(new ApiResponse("Hall has been deleted"));
    }

    @GetMapping("/get-subhalls/{hallId}")
    public ResponseEntity<?> getAllSubHalls(@PathVariable Integer hallId){
        return ResponseEntity.ok(hallService.getAllSubHalls(hallId));
    }
}
