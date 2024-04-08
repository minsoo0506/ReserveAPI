package com.mnsoo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReviewRequest {
    @JsonProperty("reviewerId")
    private String reviewerId;

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("visitedDate")
    private LocalDate visitedDate;

    @JsonProperty("visitedTime")
    private LocalTime visitedTime;

    @JsonProperty("rate")
    private Double rate;

    @JsonProperty("comment")
    private String comment;
}
