package com.mnsoo.reservation.sort;

import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.repository.StoreRepository;
import com.mnsoo.reservation.sort.distance.DistanceCalculator;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StoreSorter {

    private final StoreRepository storeRepository;

    public StoreSorter(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Page<Store> getStoresOrderedByName(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return storeRepository.findAll(pageable);
    }

    public Page<Store> getStoresOrderedByRating(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        return storeRepository.findAll(pageable);
    }

    public Page<Store> getStoresOrderedByDistance(int page, int size, double userLat, double userLng, double radius) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Store> stores = storeRepository.findAll(pageable);

        List<Store> filteredStores = stores.stream()
                .filter(store -> DistanceCalculator.calculateDistance(userLat, userLng, store.getLatitude(), store.getLongitude()) <= radius)
                .sorted(Comparator.comparing(store -> DistanceCalculator.calculateDistance(userLat, userLng, store.getLatitude(), store.getLongitude())))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredStores, pageable, filteredStores.size());
    }
}