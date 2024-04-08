package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.security.TokenProvider;
import com.mnsoo.reservation.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

// 파트너(점주)에 관한 로직을 수행하는 컨트롤러
@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;
    private final TokenProvider tokenProvider;

    // 계정 생성(점주)
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request){
        var result = this.partnerService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){
        var partner = this.partnerService.authenticate(request);
        var token = this.tokenProvider.generateToken(partner.getUserId(), partner.getRoles());

        return ResponseEntity.ok(token);
    }

    // 계정 정보 수정
    @PutMapping("/account/edit")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> edit(@RequestBody Auth.SignUp request){
        var result = this.partnerService.editUserInfo(request);
        return ResponseEntity.ok(result);
    }

    // 계정 삭제(탈퇴)
    @DeleteMapping("/account/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteAccount() {
        var result = this.partnerService.deleteAccount();
        return ResponseEntity.ok(result);
    }

    // 새로운 가게 등록
    @PostMapping("/store/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerStore(@RequestBody Store store, Authentication authentication){
        var result = this.partnerService.enrollStore(store, authentication);
        return ResponseEntity.ok(result);
    }

    // 등록된 가게의 정보를 수정
    @PutMapping("/store/edit")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> editStore(@RequestBody Store store){
        var result = this.partnerService.editStore(store);
        return ResponseEntity.ok(result);
    }

    // 등록된 가게를 삭제
    @DeleteMapping("/store/delete/{storeName}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(@PathVariable String storeName){
        boolean deleteSuccess = this.partnerService.deleteStore(storeName);
        if(deleteSuccess) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 점주 예약 정보 확인(날짜별 시간 테이블 목록)
    @GetMapping("/reserve/check")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> checkReservation(
            @RequestParam String storeName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        var result = this.partnerService.getReservations(storeName, date);
        return ResponseEntity.ok(result);
    }

    // 예약 정보 확인 후 거절(디폴트는 승인 상태)
    @PostMapping("/reserve/refuse")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> refuseReservation(
            @RequestParam String storeName,// 상점 이름
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,// 예약 날짜
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time// 예약 시간
    ){
        var result = this.partnerService.refuseReservation(storeName, date, time);
        return ResponseEntity.ok(result);
    }
}
