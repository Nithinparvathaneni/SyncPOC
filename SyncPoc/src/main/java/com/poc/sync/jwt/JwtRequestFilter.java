package com.poc.sync.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

	@Autowired
    JwtUtil jwtUtil;
    @Autowired
    UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
           throws ServletException, IOException {
       logger.debug("Inside doFilterInternal of JwtRequestFilter class");
       final String authorizationHeader = request.getHeader("Authorization");

       String username = null;
       String jwt = null;

       if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
           jwt = authorizationHeader.substring(7);
           try {
               username = jwtUtil.extractUsername(jwt);
           } catch (JwtException e) {
               logger.warn("JWT token is invalid: " + e.getMessage());
           }
       }

       if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
           UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

           if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
               UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                       new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
               usernamePasswordAuthenticationToken
                       .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
               SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
           }
       }

       chain.doFilter(request, response);
       logger.debug("Exiting doFilterInternal of JwtRequestFilter class");
   }
}
