package com.mnsoo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

// 사용자가 작성한 리뷰(데이터)를 RequestBody로 담아오기 위한 클래스
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
