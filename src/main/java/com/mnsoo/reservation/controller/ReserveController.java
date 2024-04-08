package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.ReservationRequest;
import com.mnsoo.reservation.security.TokenProvider;
import com.mnsoo.reservation.service.ReserverService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

// 예약(자) 관련 로직을 수행하는 컨트롤러
@RestController
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserverService reserverService;
    private final TokenProvider tokenProvider;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request){
        var result = this.reserverService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){
        var reserver = this.reserverService.authenticate(request);
        var token = this.tokenProvider.generateToken(reserver.getUserId(), reserver.getRoles());

        return ResponseEntity.ok(token);
    }

    // 사용자 정보 수정
    @PutMapping("/account/edit")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> edit(@RequestBody Auth.SignUp request){
        var result = this.reserverService.editUserInfo(request);
        return ResponseEntity.ok(result);
    }

    // 사용자 계정 삭제(탈퇴)
    @DeleteMapping("/account/delete")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> deleteAccount(){
        var result = this.reserverService.deleteAccount();
        return ResponseEntity.ok(result);
    }

    // 상점 이름으로 상점 상세 정보 조회
    @GetMapping("/search/store/{storeName}")
    public ResponseEntity<?> searchStore(@PathVariable String storeName){
        var result = this.reserverService.search(storeName);
        return ResponseEntity.ok(result);
    }

    // 정렬 기준대로 상점 목록 조회
    @GetMapping("/search/stores/{criteria}")
    public ResponseEntity<?> getStores(
            @PathVariable String criteria, // 정렬 기준(name, rating, distance 중 1)
            @RequestParam(defaultValue = "0") int page, // 페이지 번호
            @RequestParam(defaultValue = "10") int size, // 페이지 크기
            @RequestParam(required = false) Double userLat, // 사용자의 위도
            @RequestParam(required = false) Double userLng, // 사용자의 경도
            @RequestParam(required = false) Double radius) // 조회하고자 하는 반경(KM 단위)
    {
        var result = reserverService.sortStores(criteria, page, size, userLat, userLng, radius);
        return ResponseEntity.ok(result);
    }

    // 예약 생성
    @PostMapping("/request")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> makeReservation(@RequestBody ReservationRequest request) {
        var result = this.reserverService.makeReservation(request);
        return ResponseEntity.ok(result);
    }

    // 매장 도착 정보 확인(키오스크에서 실행)
    @GetMapping("/arrive")
    public ResponseEntity<?> arrivalConfirm(
            @RequestParam String storeName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,// 예약 날짜
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, // 예약 시간
            @RequestParam String phoneNumber// 예약자 핸드폰 번호
    ){
        var result = this.reserverService.arrivalConfirm(storeName, date, time, phoneNumber);
        return ResponseEntity.ok(result);
    }
}
