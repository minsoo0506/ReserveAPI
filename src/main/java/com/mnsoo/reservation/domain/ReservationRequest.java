package com.mnsoo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

// 사용자가 작성한 예약 정보(데이터)를 RequestBody로 담아오기 위한 클래스
@Data
public class ReservationRequest {

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("date")
    private LocalDate date; // "yyyy-MM-dd" 형식

    @JsonProperty("time")
    private LocalTime time; // "HH:mm:ss" 형식
}