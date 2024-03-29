package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.security.TokenProvider;
import com.mnsoo.reservation.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request){
        var result = this.partnerService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){
        var partner = this.partnerService.authenticate(request);
        var token = this.tokenProvider.generateToken(partner.getUserId(), partner.getRoles());

        return ResponseEntity.ok(token);
    }

    @PutMapping("/account/edit")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> edit(@RequestBody Auth.SignUp request){
        var result = this.partnerService.editUserInfo(request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/account/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteAccount() {
        var result = this.partnerService.deleteAccount();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/store/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerStore(@RequestBody Store store, Authentication authentication){
        var result = this.partnerService.enrollStore(store, authentication);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/store/edit")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> editStore(@RequestBody Store store, Authentication authentication){
        var result = this.partnerService.editStore(store, authentication);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/store/delete/{storeName}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(@PathVariable String storeName){
        var result = this.partnerService.deleteStore(storeName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/check")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> checkReservation(
            @RequestParam String storeName,
            @RequestParam LocalDate localDate
    ){
        var result = this.partnerService.getReservations(storeName, localDate);
        return ResponseEntity.ok(result);
    }
}
