package com.mnsoo.reservation.domain.persist;

import com.mnsoo.reservation.domain.Auth;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "partner")
public class PartnerEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    public void updateInfo(Auth.SignUp updatedInfo) {
        if (updatedInfo.getName() != null) {
            this.name = updatedInfo.getName();
        }
        if (updatedInfo.getUserId() != null) {
            this.userId = updatedInfo.getUserId();
        }
        if (updatedInfo.getPassword() != null) {
            this.password = updatedInfo.getPassword();
        }
        if (updatedInfo.getPhoneNumber() != null) {
            this.phoneNumber = updatedInfo.getPhoneNumber();
        }
        if (updatedInfo.getRoles() != null && !updatedInfo.getRoles().isEmpty()) {
            this.roles = updatedInfo.getRoles();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
