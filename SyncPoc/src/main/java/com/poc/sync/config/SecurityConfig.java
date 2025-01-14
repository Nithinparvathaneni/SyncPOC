package com.poc.sync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class SecurityConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
   @Bean
   public PasswordEncoder passwordEncoder() {
       logger.debug("Creating BCryptPasswordEncoder bean.");
       return new BCryptPasswordEncoder();
   }
   protected void configure(HttpSecurity http) throws Exception {
	   try {
           logger.debug("Configuring HttpSecurity.");
           http.csrf().disable()
           .authorizeRequests()
           .requestMatchers("/api/users/register", "/api/users/login","/api/users/token").permitAll()
           .requestMatchers( "/api/users/update-phone","/api/users/upload-image","/api/users/images", "/api/users/delete-image", "/api/users/profile-with-images","/api/users/image/{imageId}").authenticated()
           .anyRequest().authenticated()
           .and()
           .httpBasic();
           logger.info("HttpSecurity configured successfully.");
       } catch (Exception e) {
           logger.error("Error configuring HttpSecurity.", e);
           throw e;
       }
   }
   
}
