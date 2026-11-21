package com.raza.kisanmitra.config;

import com.raza.kisanmitra.repository.FarmerUserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(FarmerUserRepository users){return email->users.findByEmail(email.toLowerCase()).map(u->User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles("FARMER").build()).orElseThrow(()->new UsernameNotFoundException("Farmer not found"));}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
  return http.authorizeHttpRequests(a->a.requestMatchers("/login","/register","/css/**","/js/**","/assets/**","/error","/actuator/health").permitAll().anyRequest().authenticated())
   .formLogin(f->f.loginPage("/login").usernameParameter("email").defaultSuccessUrl("/dashboard",true).permitAll())
   .logout(l->l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
   .build();
 }
}
