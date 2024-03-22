package com.mnsoo.reservation.repository;

import com.mnsoo.reservation.domain.persist.ReserverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReserverRepository extends JpaRepository<ReserverEntity, Long> {
    Optional<ReserverEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
