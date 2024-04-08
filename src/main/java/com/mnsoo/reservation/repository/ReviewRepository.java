package com.mnsoo.reservation.repository;

import com.mnsoo.reservation.domain.persist.Review;
import com.mnsoo.reservation.domain.persist.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStoreName(String storeName);

    Optional<Review> findByReviewerIdAndStoreNameAndVisitedDateAndVisitedTime(
            String reviewerId,
            String storeName,
            LocalDate visitedDate,
            LocalTime visitedTime
    );

    List<Review> findByStore(Store store);
}
