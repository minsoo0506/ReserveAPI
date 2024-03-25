package com.mnsoo.reservation.repository;

import com.mnsoo.reservation.domain.persist.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);

    boolean existsByName(String name);


}
