package com.mnsoo.reservation.service;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.ReservationRequest;
import com.mnsoo.reservation.domain.persist.Reservation;
import com.mnsoo.reservation.domain.persist.ReserverEntity;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.exception.impl.AlreadyExistUserException;
import com.mnsoo.reservation.exception.impl.StoreNotFoundException;
import com.mnsoo.reservation.repository.ReservationRepository;
import com.mnsoo.reservation.repository.ReserverRepository;
import com.mnsoo.reservation.repository.StoreRepository;
import com.mnsoo.reservation.sort.StoreSorter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ReserverService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final ReserverRepository reserverRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final StoreSorter storeSorter;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return this.reserverRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + userId));
    }

    public ReserverEntity register(Auth.SignUp reserver){
        boolean exists = this.reserverRepository.existsByUserId(reserver.getUserId());
        if(exists) {
            throw new AlreadyExistUserException();
        }

        reserver.setPassword(this.passwordEncoder.encode(reserver.getPassword()));
        var result = this.reserverRepository.save(reserver.toReserverEntity());
        return result;
    }

    public ReserverEntity editUserInfo(Auth.SignUp reserver){
        ReserverEntity currentReserver = getCurrentReserver();
        currentReserver.updateInfo(reserver);
        reserverRepository.save(currentReserver);
        return currentReserver;
    }

    public String deleteAccount(){
        ReserverEntity currentReserver = getCurrentReserver();
        reserverRepository.delete(currentReserver);
        return "Account deleted successfully";
    }

    public ReserverEntity authenticate(Auth.SignIn reserver){
        var user = this.reserverRepository.findByUserId(reserver.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));

        if(!this.passwordEncoder.matches(reserver.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public Store search(String storeName){
        var store = this.storeRepository.findByName(storeName)
                .orElseThrow(StoreNotFoundException::new);

        return store;
    }

    public Page<Store> sortStores(String criteria, int page, int size, Double userLat, Double userLng, Double radius) {
        return switch (criteria) {
            case "name" -> storeSorter.getStoresOrderedByName(page, size);
            case "rating" -> storeSorter.getStoresOrderedByRating(page, size);
            case "distance" -> {
                if (userLat == null || userLng == null || radius == null) {
                    throw new IllegalArgumentException("User latitude, longitude and radius must be provided for distance sort");
                }
                yield storeSorter.getStoresOrderedByDistance(page, size, userLat, userLng, radius);
            }
            default ->
                    throw new IllegalArgumentException("Invalid sort criteria");
        };
    }

    public Reservation makeReservation(ReservationRequest request) {
        ReserverEntity currentReserver = getCurrentReserver();

        Store store = this.storeRepository.findByName(request.getStoreName())
                .orElseThrow(StoreNotFoundException::new);

        LocalDate reservationDate = request.getDate();

        Optional<Reservation> existingReservation = this.reservationRepository
                .findByStoreAndReservationDate(store, reservationDate);
        if (existingReservation.isPresent()) {
            throw new RuntimeException("There is already a reservation at this time.");
        }

        Reservation reservation = Reservation.builder()
                .reserverPhoneNumber(currentReserver.getPhoneNumber())
                .reservationDate(request.getDate())
                .reservationTime(request.getTime())
                .store(store)
                .status(true)
                .build();

        Reservation savedReservation = this.reservationRepository.save(reservation);

        return savedReservation;
    }

    public String arrivalConfirm(String storeName, LocalDate date, LocalTime time, String phoneNumber){
        Optional<Reservation> optionalReservation = this.reservationRepository
                .findByStore_NameAndReservationDateAndReservationTimeAndReserverPhoneNumber(
                        storeName,
                        date,
                        time,
                        phoneNumber
                );

        if (optionalReservation.isEmpty()) {
            throw new RuntimeException("No such reservation");
        }

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (!today.equals(date)
                || currentTime.isAfter(time)
                || currentTime.plusMinutes(10).isAfter(time)
                || optionalReservation.get().getStatus() == false
        ) {
            throw new RuntimeException("Invalid Confirmation");
        }
        return "confirmed!";
    }

    private ReserverEntity getCurrentReserver() {
        return (ReserverEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
