package com.mnsoo.reservation.domain;

import com.mnsoo.reservation.domain.persist.PartnerEntity;
import com.mnsoo.reservation.domain.persist.ReserverEntity;
import lombok.Data;

import java.util.List;

// 인증과 관련된 데이터를 표현하는 클래스.
// SignIn과 SignUp 두 개의 내부 클래스를 가짐.
// SignIn 클래스는 사용자가 로그인할 때 사용하는 ID와 비밀번호를 표현.
// SignUp 클래스는 사용자가 회원가입할 때 필요한 정보를 표현하며, 이 정보를 기반으로 PartnerEntity와 ReserverEntity를 생성하는 메소드를 제공.
public class Auth {
    @Data
    public static class SignIn{
        private String userId;
        private String password;
    }

    @Data
    public static class SignUp{
        private String name;
        private String userId;
        private String password;
        private String phoneNumber;
        private List<String> roles;
        public PartnerEntity toPartnerEntity(){
            return PartnerEntity.builder()
                    .name(this.name)
                    .userId(this.userId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .roles(this.roles)
                    .build();
        }

        public ReserverEntity toReserverEntity(){
            return ReserverEntity.builder()
                    .name(this.name)
                    .userId(this.userId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .roles(this.roles)
                    .build();
        }
    }
}
