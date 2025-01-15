package com.poc.sync.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImgurService {

  private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);

  private final RestTemplate restTemplate = new RestTemplate();

  //private String accessToken = "67ffbe572c942e63fd1b59307eb441d955dd1c0d";
  
  @Value("${imgur.access.token}")
  private String accessToken;

  public String uploadImage(MultipartFile file) throws Exception {
	  String url = "https://api.imgur.com/3/image";
      try {
          logger.debug("Starting image upload to Imgur");

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.MULTIPART_FORM_DATA);
          headers.set("Authorization", "Bearer " + accessToken);

          MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
          body.add("image", file.getResource());

          HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
          ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

          if (response.getStatusCode() == HttpStatus.OK) {
              Map<String, Object> responseBody = response.getBody();
              if (responseBody != null && responseBody.containsKey("data")) {
                  Map<String, String> data = (Map<String, String>) responseBody.get("data");
                  String imageUrl = data.get("link");
                  logger.info("Image uploaded successfully to Imgur: {}", imageUrl);
                  return imageUrl;
              } else {
                  logger.error("Imgur response did not contain 'data' key");
                  throw new Exception("Failed to upload image to Imgur");
              }
          } else {
              logger.error("Failed to upload image to Imgur, status code: {}", response.getStatusCode());
              throw new Exception("Failed to upload image to Imgur");
          }
      } catch (Exception e) {
          logger.error("Error occurred while uploading image to Imgur", e);
          throw new RuntimeException("Failed to upload image to Imgur. Please try again later.", e);
      }
  }
  
  public void deleteImage(String deleteHash) {
	  String url = "https://api.imgur.com/3/image/" + deleteHash;
      try {
          logger.debug("Attempting to delete image with deleteHash: {}", deleteHash);

          HttpHeaders headers = new HttpHeaders();
          headers.set("Authorization", "Bearer " + accessToken);

          HttpEntity<String> entity = new HttpEntity<>(headers);
          ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

          if (response.getStatusCode() == HttpStatus.OK) {
              logger.info("Image deleted successfully with deleteHash: {}", deleteHash);
          } else {
              logger.error("Failed to delete image with deleteHash: {}. Status code: {}", deleteHash, response.getStatusCode());
              throw new RuntimeException("Failed to delete image");
          }
      } catch (Exception e) {
          logger.error("Error occurred while deleting image with deleteHash: {}", deleteHash, e);
          throw new RuntimeException("Failed to delete image. Please try again later.", e);
      }
  }

  public String getImage(String imageId) throws Exception {
	  String url = "https://api.imgur.com/3/image/" + imageId;
      try {
          logger.debug("Fetching image with imageId: {}", imageId);

          HttpHeaders headers = new HttpHeaders();
          headers.set("Authorization", "Bearer " + accessToken);

          HttpEntity<String> entity = new HttpEntity<>(headers);
          ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

          if (response.getStatusCode() == HttpStatus.OK) {
              Map<String, Object> responseBody = response.getBody();
              if (responseBody != null && responseBody.containsKey("data")) {
                  Map<String, String> data = (Map<String, String>) responseBody.get("data");
                  String imageUrl = data.get("link");
                  logger.info("Image fetched successfully with imageId: {}", imageId);
                  return imageUrl;
              } else {
                  logger.error("Imgur response did not contain 'data' key for imageId: {}", imageId);
                  throw new Exception("Failed to get image from Imgur");
              }
          } else {
              logger.error("Failed to get image from Imgur, status code: {}", response.getStatusCode());
              throw new Exception("Failed to get image from Imgur");
          }
      } catch (Exception e) {
          logger.error("Error occurred while fetching image with imageId: {}", imageId, e);
          throw new RuntimeException("Failed to get image from Imgur. Please try again later.", e);
      }
  }
}
