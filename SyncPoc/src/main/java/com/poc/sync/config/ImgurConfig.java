package com.poc.sync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImgurConfig {
	
   private static final Logger logger = LoggerFactory.getLogger(ImgurConfig.class);

   @Value("${imgur.client.id}")
   private String clientId;

   @Value("${imgur.client.secret}")
   private String clientSecret;

   public String getClientId() {
	      if (clientId == null || clientId.isEmpty()) {
	          logger.error("Imgur client ID is not configured properly.");
	          throw new IllegalStateException("Imgur client ID is not configured properly.");
	      }
	      logger.debug("Returning Imgur client ID.");
	      return clientId;
	  }

   public String getClientSecret() {
	      if (clientSecret == null || clientSecret.isEmpty()) {
	          logger.error("Imgur client secret is not configured properly.");
	          throw new IllegalStateException("Imgur client secret is not configured properly.");
	      }
	      logger.debug("Returning Imgur client secret.");
	      return clientSecret;
	  }
}
