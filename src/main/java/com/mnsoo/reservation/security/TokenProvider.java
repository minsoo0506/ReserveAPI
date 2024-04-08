package com.mnsoo.reservation.security;

import com.mnsoo.reservation.service.PartnerService;
import com.mnsoo.reservation.service.ReserverService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

// TokenProvider는 JWT 토큰을 생성하고 검증하는 역할을 수행.
// 토큰은 사용자의 ID와 역할 정보를 포함하며, 이 정보는 HTTP 요청을 처리하는 동안 인증과 권한 확인에 사용됨.
// 이 클래스는 토큰을 생성(generateToken), 토큰에서 인증 정보를 추출(getPartnerAuthentication, getReserverAuthentication),
// 토큰에서 사용자 이름을 추출(getUsername), 토큰의 유효성을 검증(validateToken), 토큰에서 역할 정보를 추출(getRolesFromToken)하는 메소드를 제공함.
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
    private static final String KEY_ROLES = "roles";

    private final PartnerService partnerService;
    private final ReserverService reserverService;

    @Value("{spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     * @param userId
     * @param roles
     * @return String
     */
    public String generateToken(String userId, List<String> roles){
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put(KEY_ROLES, roles);

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
                .compact();
    }

    public Authentication getPartnerAuthentication(String jwt){
        String username = this.getUsername(jwt);
        UserDetails userDetails = this.partnerService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Authentication getReserverAuthentication(String jwt){
        String username = this.getUsername(jwt);
        UserDetails userDetails = this.reserverService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token){
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token){
        if(!StringUtils.hasText(token)) return false; // 토큰의 값이 빈 값인 경우

        var claims = this.parseClaims(token);
        // 토큰의 만료시간이 현재의 시간보다 이전인지 아닌지 확인
        return !claims.getExpiration().before(new Date());
    }

    private Claims parseClaims(String token){
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e){
            // TODO
            return e.getClaims();
        }
    }

    public String getRolesFromToken(String token){
        Claims claims = this.parseClaims(token);
        List<String> roles = (List<String>) claims.get(KEY_ROLES);

        return roles.isEmpty() ? null : roles.get(0);
    }
}
