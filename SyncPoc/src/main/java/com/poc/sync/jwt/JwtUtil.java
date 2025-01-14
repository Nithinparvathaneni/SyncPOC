package com.poc.sync.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
	
   private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

   @Value("${jwt.secret}")
   private String secret;

   @Value("${jwt.expiration}")
   private long expiration;

   public String generateToken(String username) {
	   try {
           logger.debug("Generating token for username: {}", username);
           return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + expiration))
                   .signWith(SignatureAlgorithm.HS512, secret)
                   .compact();
       } catch (Exception e) {
           logger.error("Error occurred while generating token for username: {}", username, e);
           throw new RuntimeException("Failed to generate token. Please try again later.", e);
       }
   }

   public String extractUsername(String token) {
	   logger.debug("Extracting username from token");
       return extractClaim(token, Claims::getSubject);
   }

   public Date extractExpiration(String token) {
	   logger.debug("Extracting expiration date from token");
       return extractClaim(token, Claims::getExpiration);
   }

   public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
	   logger.debug("Extracting claim from token");
       final Claims claims = extractAllClaims(token);
       return claimsResolver.apply(claims);
   }

   private Claims extractAllClaims(String token) {
	   logger.debug("Extracting all claims from token");
       return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
   }

   private Boolean isTokenExpired(String token) {
	   logger.debug("Checking if token is expired");
       return extractExpiration(token).before(new Date());
   }

   public Boolean validateToken(String token, String username) {
	   logger.debug("Validating token for username: {}", username);
       final String extractedUsername = extractUsername(token);
       return (extractedUsername.equals(username) && !isTokenExpired(token));
   }
}
