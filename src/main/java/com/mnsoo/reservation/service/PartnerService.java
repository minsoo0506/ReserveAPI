package com.mnsoo.reservation.service;

import com.mnsoo.reservation.domain.Auth;
import com.mnsoo.reservation.domain.persist.PartnerEntity;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.exception.impl.AlreadyExistStoreException;
import com.mnsoo.reservation.exception.impl.AlreadyExistUserException;
import com.mnsoo.reservation.repository.PartnerRepository;
import com.mnsoo.reservation.repository.StoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PartnerService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final PartnerRepository partnerRepository;
    private final StoreRepository storeRepository;

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
        PartnerEntity currentPartner = (PartnerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        currentPartner.updateInfo(partner);
        partnerRepository.save(currentPartner);
        return currentPartner;
    }

    public String deleteAccount() {
        PartnerEntity currentPartner = (PartnerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    public Store enroll(Store store){
        boolean exist = this.storeRepository.existsByName(store.getName());
        if(exist){
            throw new AlreadyExistStoreException();
        }

        var result = this.storeRepository.save(store);
        return result;
    }
}
