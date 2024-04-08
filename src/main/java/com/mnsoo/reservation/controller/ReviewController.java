package com.mnsoo.reservation.controller;

import com.mnsoo.reservation.domain.ReviewRequest;
import com.mnsoo.reservation.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest createRequest){
        var result = this.reviewService.createReview(createRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> updateReview(@RequestBody ReviewRequest updateRequest){
        var result = this.reviewService.updateReview(updateRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('RESERVER')")
    public ResponseEntity<?> deleteReview(@RequestBody ReviewRequest deleteRequest){
        var result = this.reviewService.deleteReview(deleteRequest);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admin/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteReviewByAdmin(
            @RequestParam String storeName,
            @RequestParam String userId,
            @RequestParam String partnerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    ){
        var result = this.reviewService.deleteReviewByAdmin(storeName, userId, partnerId, date, time);
        return ResponseEntity.ok(result);
    }
}
