package com.mnsoo.reservation.sort;

import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.repository.StoreRepository;
import com.mnsoo.reservation.sort.distance.DistanceCalculator;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// 상점 목록을 조회함에 있어서 정렬(가나다순, 별점순, 거리순)을 위한 클래스
@Component
public class StoreSorter {

    private final StoreRepository storeRepository;

    public StoreSorter(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    // 가나다 순으로 정렬
    public Page<Store> getStoresOrderedByName(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return storeRepository.findAll(pageable);
    }

    // 별점 순으로 정렬
    public Page<Store> getStoresOrderedByRating(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        return storeRepository.findAll(pageable);
    }

    // 거리가 가까운 순으로 정렬

    /**
     *
     * @param page
     * @param size
     * @param userLat (사용자 위치(위도))
     * @param userLng (사용자 위치(경도))
     * @param radius (조회하고자 하는 반경(Km))
     * @return
     */
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