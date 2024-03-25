package com.mnsoo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationRequest {

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("date")
    private LocalDate date; // "yyyy-MM-dd" 형식

    @JsonProperty("time")
    private LocalTime time; // "HH:mm:ss" 형식
}