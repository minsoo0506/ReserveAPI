package com.mnsoo.reservation.config;

import com.mnsoo.reservation.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 웹 보안 설정을 담당하는 클래스.
// Spring Security의 설정을 확장하며, HTTP 요청에 대한 보안 요구사항을 정의함.
// 이 설정에는 HTTP Basic 인증 비활성화, CSRF 보호 비활성화, 세션 관리 전략 설정,
// 특정 엔드포인트에 대한 인증 요구 제외, 그 외 모든 요청에 대한 인증 요구 등이 포함됨.
// 또한, JWT 인증 필터를 Security Filter Chain에 추가.
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtAuthenticationFilter authenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                        .antMatchers("/**/signup", "/**/signin", "/**/search/**", "/**/arrive/**").permitAll()  // 엔드포인트에 대한 인증 요구 제외
                .anyRequest().authenticated()  // 그 외 모든 요청에 대해 인증 요구
                .and()
                .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }
}