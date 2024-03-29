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
import java.util.Iterator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class PartnerService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final PartnerRepository partnerRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return this.partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + userId));
    }

    public PartnerEntity register(Auth.SignUp partner) {
        boolean exists = this.partnerRepository.existsByUserId(partner.getUserId());
        if(exists) {
            throw new AlreadyExistUserException();
        }

        partner.setPassword(this.passwordEncoder.encode(partner.getPassword()));
        var result = this.partnerRepository.save(partner.toPartnerEntity());
        return result;
    }

    public PartnerEntity editUserInfo(Auth.SignUp partner) {
        PartnerEntity currentPartner = getCurrentPartner();

        if (partner.getPassword() != null) {
            partner.setPassword(this.passwordEncoder.encode(partner.getPassword()));
        }

        currentPartner.updateInfo(partner);
        partnerRepository.save(currentPartner);
        return currentPartner;
    }

    public String deleteAccount() {
        PartnerEntity currentPartner = getCurrentPartner();
        partnerRepository.delete(currentPartner);
        return "Account deleted successfully";
    }

    public PartnerEntity authenticate(Auth.SignIn partner){
        var user = this.partnerRepository.findByUserId(partner.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다"));

        if(!this.passwordEncoder.matches(partner.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public Store enrollStore(Store store, Authentication authentication){
        String currentUserId = authentication.getName();
        PartnerEntity currentPartner = partnerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        boolean exist = this.storeRepository.existsByName(store.getName());
        if(exist){
            throw new AlreadyExistStoreException();
        }

        var result = this.storeRepository.save(store);
        currentPartner.addStore(store);
        return result;
    }

    public Store editStore(Store updatedStore){
        Store existingStore = storeRepository.findByName(updatedStore.getName())
                .orElseThrow(() -> new RuntimeException("Store not found"));

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

        var result = this.storeRepository.save(existingStore);
        return result;
    }

    @Transactional
    public boolean deleteStore(String storeName){
        PartnerEntity currentPartner = partnerRepository.findByIdAndInitializeStores(getCurrentPartner().getId());
        for(Iterator<Store> iterator = currentPartner.getStores().iterator(); iterator.hasNext();){
            Store store = iterator.next();
            if(store.getName().equals(storeName)){
                iterator.remove();
                storeRepository.delete(store);
                storeRepository.flush();
                return true;
            }
        }
        return false;
    }

    public List<Reservation> getReservations(String storeName, LocalDate date){
        return reservationRepository.findByStoreNameAndReservationDateOrderByReservationTimeAsc(storeName, date);
    }

    private PartnerEntity getCurrentPartner() {
        return (PartnerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
