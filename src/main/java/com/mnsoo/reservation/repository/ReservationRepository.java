package com.mnsoo.reservation.repository;

import com.mnsoo.reservation.domain.persist.Reservation;
import com.mnsoo.reservation.domain.persist.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByStoreAndReservationTime(Store store, LocalDateTime reservationTime);

    List<Reservation> findByStoreNameAndReservationDateOrderByReservationTimeAsc(String storeName, LocalDate reservationDate);
}
