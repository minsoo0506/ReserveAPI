package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.ReservationRequest;
import com.mnsoo.reservation.security.TokenProvider;
import com.mnsoo.reservation.service.ReserverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserverService reserverService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request){
        var result = this.reserverService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){
        var reserver = this.reserverService.authenticate(request);
        var token = this.tokenProvider.generateToken(reserver.getUserId(), reserver.getRoles());

        return ResponseEntity.ok(token);
    }

    @PutMapping("/account/edit")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> edit(@RequestBody Auth.SignUp request){
        var result = this.reserverService.editUserInfo(request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/account/delete")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> deleteAccount(){
        var result = this.reserverService.deleteAccount();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/store/{storeName}")
    public ResponseEntity<?> searchStore(@PathVariable String storeName){
        var result = this.reserverService.search(storeName);
        return ResponseEntity.ok(result);
    }

    // Need to check
    @GetMapping("/search/stores/{criteria}")
    public ResponseEntity<?> getStores(
            @PathVariable String criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(required = false) Double radius)
    {
        var result = reserverService.sortStores(criteria, page, size, userLat, userLng, radius);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> makeReservation(@RequestBody ReservationRequest request) {
        var result = this.reserverService.makeReservation(request);
        return ResponseEntity.ok(result);
    }
}
