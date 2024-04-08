package com.mnsoo.reservation.service;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.persist.PartnerEntity;
import com.mnsoo.reservation.domain.persist.Reservation;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.exception.impl.AlreadyExistStoreException;
import com.mnsoo.reservation.exception.impl.AlreadyExistUserException;
import com.mnsoo.reservation.repository.PartnerRepository;
import com.mnsoo.reservation.repository.ReservationRepository;
import com.mnsoo.reservation.repository.StoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

// 파트너(점주)에 관한 로직을 수행하는 서비스 클래스
@Service
@AllArgsConstructor
@Transactional
public class PartnerService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final PartnerRepository partnerRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    // UserDetails를 로드하는 메소드
    // Spring Security에서 사용자 인증을 처리하는 데 사용
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return this.partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + userId));
    }

    // 사용자(파트너, 점주) 등록을 위한 메소드
    public PartnerEntity register(Auth.SignUp partner) {
        // 이미 존재하는 ID인지 확인
        boolean exists = this.partnerRepository.existsByUserId(partner.getUserId());
        if(exists) {
            throw new AlreadyExistUserException();
        }

        // 사용자의 비밀번호를 암호화하여 저장
        // 비밀번호를 평문으로 저장되지 않아 보안을 유지함
        partner.setPassword(this.passwordEncoder.encode(partner.getPassword()));
        var result = this.partnerRepository.save(partner.toPartnerEntity());
        return result;
    }

    // 사용자 정보 수정을 위한 메소드
    public PartnerEntity editUserInfo(Auth.SignUp partner) {
        PartnerEntity currentPartner = getCurrentPartner();

        if (partner.getPassword() != null) {
            partner.setPassword(this.passwordEncoder.encode(partner.getPassword()));
        }

        currentPartner.updateInfo(partner);
        partnerRepository.save(currentPartner);
        return currentPartner;
    }

    // 사용자 계정을 삭제하기 위한 메소드
    public String deleteAccount() {
        PartnerEntity currentPartner = getCurrentPartner();
        partnerRepository.delete(currentPartner);
        return "Account deleted successfully";
    }

    // 사용자에 대한 정보가 유효한지 확인하는 메소드
    public PartnerEntity authenticate(Auth.SignIn partner){
        var user = this.partnerRepository.findByUserId(partner.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다"));

        if(!this.passwordEncoder.matches(partner.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // 상점 등록을 위한 메소드
    public Store enrollStore(Store store, Authentication authentication){
        // 현재 인증된 사용자의 ID를 가져오기
        String currentUserId = authentication.getName();
        // 현재 인증된 사용자가 파트너인지 확인하고, 파트너 정보 조회하기
        PartnerEntity currentPartner = partnerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // 상점 이름이 이미 존재하는지 확인. 만약 존재한다면, 예외를 발생시킴.
        boolean exist = this.storeRepository.existsByName(store.getName());
        if(exist){
            throw new AlreadyExistStoreException();
        }

        // 상점 정보를 저장하고, 현재 파트너에게 상점을 추가
        var result = this.storeRepository.save(store);
        currentPartner.addStore(store);
        return result;
    }

    // 상점에 대한 정보를 수정하는 메소드
    public Store editStore(Store updatedStore){
        // 업데이트할 상점을 찾기. 만약 찾을 수 없다면, 예외를 발생시킴.
        Store existingStore = storeRepository.findByName(updatedStore.getName())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // 상점 정보를 업데이트
        if(updatedStore.getName() != null) {
            existingStore.setName(updatedStore.getName());
        }
        if(updatedStore.getLocation() != null) {
            existingStore.setLocation(updatedStore.getLocation());
        }
        if(updatedStore.getLatitude() != null) {
            existingStore.setLatitude(updatedStore.getLatitude());
        }
        if(updatedStore.getLocation() != null) {
            existingStore.setLongitude(updatedStore.getLongitude());
        }
        if(updatedStore.getDescription() != null) {
            existingStore.setDescription(updatedStore.getDescription());
        }

        // 업데이트된 상점 정보를 저장하고 반환
        var result = this.storeRepository.save(existingStore);
        return result;
    }

    // 상점을 삭제하는 메소드
    @Transactional
    public boolean deleteStore(String storeName){
        // 현재 파트너를 가져오기
        PartnerEntity currentPartner = partnerRepository.findByIdAndInitializeStores(getCurrentPartner().getId());
        // 파트너가 소유한 모든 상점을 순회하며, 삭제할 상점을 찾기(한 점주가 몇십개, 몇백개의 상점을 가지고 있는 경우는 거의 없으므로)
        for(Iterator<Store> iterator = currentPartner.getStores().iterator(); iterator.hasNext();){
            Store store = iterator.next();
            if(store.getName().equals(storeName)){
                // 삭제할 상점을 찾았다면, 상점을 삭제하고 true를 반환
                iterator.remove();
                storeRepository.delete(store);
                storeRepository.flush();
                return true;
            }
        }
        // 삭제할 상점을 찾지 못했다면, false를 반환
        return false;
    }

    // 주어진 상점 이름과 예약 날짜에 해당하는 모든 예약을 가져오는 메소드
    public List<Reservation> getReservations(String storeName, LocalDate date){
        return reservationRepository.findByStoreNameAndReservationDateOrderByReservationTimeAsc(storeName, date);
    }

    // 예약 정보 확인 후, 예약 거절을 수행하는 메소드
    public Reservation refuseReservation(String storeName, LocalDate date, LocalTime time){
        // 주어진 상점 이름, 예약 날짜, 예약 시간에 해당하는 예약 조회
        Optional<Reservation> reservation = reservationRepository.findByStore_NameAndReservationDateAndReservationTime(
                storeName,
                date,
                time
        );

        // 해당 예약이 없다면, 예외를 발생시킴.
        if(reservation.isEmpty()){
            throw new RuntimeException("No Such Reservation");
        }

        // 예약 상태를 false로 설정하고, 예약 정보를 저장
        reservation.get().setStatus(false);
        reservationRepository.save(reservation.get());

        return reservation.get();
    }

    // 현재 인증된 파트너를 반환하는 메소드
    private PartnerEntity getCurrentPartner() {
        return (PartnerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
