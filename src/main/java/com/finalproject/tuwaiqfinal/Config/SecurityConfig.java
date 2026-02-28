package com.finalproject.tuwaiqfinal.Config;

import com.finalproject.tuwaiqfinal.Service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final MyUserDetailsService myUserDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(myUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(daoAuthenticationProvider())
                .authorizeHttpRequests()
                // Permit All
                .requestMatchers("/api/v1/customer/register", "/api/v1/owner/register").permitAll()
                .requestMatchers("/api/v1/payments/callback").permitAll()
                .requestMatchers("/api/v1/review-hall/getAll").permitAll()
                //Login
                .requestMatchers("/api/v1/auth/login").permitAll()

                // Shared Access (Admin, Customer, Owner)
                .requestMatchers("/api/v1/hall/get", "/api/v1/hall/get/{hallId}", "/api/v1/hall/get-subhalls/{hallId}").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/game/get").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/hall/get/available").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/hall/get/unavailable").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/hall/get/asset/**").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/subhall/get/asset/**").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/review-hall/get/asset/**").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")
                .requestMatchers("/api/v1/review-sub-hall/get/asset/**").hasAnyAuthority("ADMIN", "CUSTOMER", "OWNER")

                // Customer Authority
                .requestMatchers("/api/v1/customer/get").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/customer/update").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/customer/delete").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/customer/game/analyse").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/customer/booking/advice").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/booking/get").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/booking/add/subhall/{subhallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/booking/update/booking/{bookingId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/booking/delete/booking/{bookingId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/customer/cancel/booking/{bookingId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-hall/add/{hallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-hall/update/{hallId}/{reviewHallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-hall/delete/{hallId}/{reviewHallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-sub-hall/add/{subHallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-sub-hall/update/{subHallId}/{reviewSubHallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-sub-hall/delete/{subHallId}/{reviewSubHallId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/pay/for/{bookingId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/card").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/get/status/{paymentId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/download/invoice/{bookingId}").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-hall/add/asset/**").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/review-sub-hall/add/asset/**").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/get/all").hasAuthority("CUSTOMER")
                .requestMatchers("/api/v1/payments/get/status/filter/{status}").hasAuthority("CUSTOMER")

                // Owner Authority
                .requestMatchers("/api/v1/hall/get/my").hasAuthority("OWNER")
                .requestMatchers("/api/v1/booking/initiated/hall/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/booking/approved/hall/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/booking/remind-unpaid/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/game/add/{hallId}/{subHallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/game/update/{hallId}/{subHallId}/{gameId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/game/delete/{hallId}/{subHallId}/{gameId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/hall/add").hasAuthority("OWNER")
                .requestMatchers("/api/v1/hall/update/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/hall/delete/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/update").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/delete").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/get").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/feedback/hall/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/feedback/subhall/{subHallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/owner/cancel/booking/{bookingId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/add/{hallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/update/{subHallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/delete/{subHallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/get/{subHallId}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/hall/{hallId}/subhall/{subHallId}/budget/{pricePerHour}").hasAuthority("OWNER")
                .requestMatchers("/api/v1/hall/add/asset/**").hasAuthority("OWNER")
                .requestMatchers("/api/v1/subhall/add/asset/**").hasAuthority("OWNER")

                // Admin Authority
                .requestMatchers("/api/v1/owner/get-all").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/review-hall/get").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/review-hall/hall/{hallId}/rating").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/review-sub-hall/get").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/customer/getall").hasAuthority("ADMIN")
                .requestMatchers("/docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }


}
