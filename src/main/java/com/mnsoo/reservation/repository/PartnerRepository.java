package com.mnsoo.reservation.repository;

import com.mnsoo.reservation.domain.persist.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<PartnerEntity, Long> {
    Optional<PartnerEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Query("SELECT p FROM partner p JOIN FETCH p.stores WHERE p.id = :id")
    PartnerEntity findByIdAndInitializeStores(@Param("id") Long id);
}
