package com.mnsoo.reservation.domain.persist;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

// 리뷰에 대한 정보를 담는 엔티티 클래스
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reviewerId;

    @Column(nullable = false)
    private LocalDate visitedDate;

    @Column(nullable = false)
    private LocalTime visitedTime;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "store_name", referencedColumnName = "name")
    private Store store;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
