package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.security.TokenProvider;
import com.mnsoo.reservation.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/store/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerStore(@RequestBody Store store){
        var result = this.partnerService.enroll(store);
        return ResponseEntity.ok(result);
    }
}
