package com.poc.sync.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.sync.jwt.JwtUtil;
import com.poc.sync.model.LoginRequest;
import com.poc.sync.model.User;
import com.poc.sync.repository.UserRepository;
import com.poc.sync.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
   private static final Logger logger = LoggerFactory.getLogger(UserController.class);

   @Autowired
   private UserService userService;
   
   @Autowired
   private UserRepository userRepository;
   
   @Autowired
   private JwtUtil jwtUtil;


   @PostMapping("/register")
   public ResponseEntity<String> registerUser(@RequestBody User user) {
	   try {
       logger.debug("Attempting to register user: {}", user.getUsername());
       String message = userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail(), user.getPhoneNumber());
       
       if ("User registered successfully".equals(message)) {
           logger.info("User registered successfully: {}", user.getUsername());
           return ResponseEntity.ok(message);
       } else {
           logger.warn("User registration failed: {}", message);
           return ResponseEntity.badRequest().body(message);
       }
   } catch (Exception e) {
       logger.error("Error occurred during user registration for: {}", user.getUsername(), e);
       return ResponseEntity.status(500).body("An error occurred during registration. Please try again later.");
   		}
   }
      

   @PostMapping("/login")
   public ResponseEntity<String> loginUser(@RequestBody User user) {
	   try {
           logger.debug("Attempting to authenticate user: {}", user.getUsername());
           String message = userService.authenticateUser(user.getUsername(), user.getPassword());
           
           if ("Login successful".equals(message)) {
               logger.info("User authenticated successfully: {}", user.getUsername());
               return ResponseEntity.ok(message);
           } else {
               logger.warn("User authentication failed for: {}", user.getUsername());
               return ResponseEntity.status(401).body(message); // Unauthorized status for failed login
           }
       } catch (Exception e) {
           logger.error("Error occurred during user authentication for: {}", user.getUsername(), e);
           return ResponseEntity.status(500).body("An error occurred during authentication. Please try again later.");
       }
   }
   
   
   @PostMapping("/token")
   public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
	   try {
           logger.debug("Attempting to authenticate user: {}", loginRequest.getUsername());

           User user = userRepository.findByUsername(loginRequest.getUsername())
                   .orElseThrow(() -> {
                       logger.warn("User not found: {}", loginRequest.getUsername());
                       return new RuntimeException("User not found");
                   });

           BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
           if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
               logger.warn("Invalid credentials for user: {}", loginRequest.getUsername());
               throw new RuntimeException("Invalid credentials");
           }

           String token = jwtUtil.generateToken(user.getUsername());
           logger.info("Token generated successfully for user: {}", user.getUsername());

           Map<String, String> response = new HashMap<>();
           response.put("token", token);
           return response;

       } catch (RuntimeException e) {
           logger.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
           Map<String, String> response = new HashMap<>();
           response.put("error", e.getMessage());
           return response;  // Unauthorized status for failed login
       } catch (Exception e) {
           logger.error("Error occurred during authentication for user: {}", loginRequest.getUsername(), e);
           Map<String, String> response = new HashMap<>();
           response.put("error", "An error occurred during authentication. Please try again later.");
           return response;  // Internal server error for unexpected exceptions
       }
   }
   

   @PutMapping("/update-phone")
   public ResponseEntity<String> updatePhoneNumber(@RequestBody String phoneNumber) {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body("User not authenticated");
           }

           logger.debug("Attempting to update phone number for user: {}", username);
           String message = userService.updatePhoneNumber(username, phoneNumber);
           logger.info("Phone number updated successfully for user: {}", username);
           return ResponseEntity.ok(message);
       } catch (Exception e) {
           logger.error("Error occurred while updating phone number for user: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body("An error occurred while updating the phone number. Please try again later.");
       }
   }

   @PostMapping("/upload-image")
   public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body("User not authenticated");
           }

           logger.debug("Attempting to upload image for user: {}", username);
           String message = userService.uploadImage(username, file);

           if ("Image uploaded successfully".equals(message)) {
               logger.info("Image uploaded successfully for user: {}", username);
               return ResponseEntity.ok(message);
           } else {
               logger.warn("Image upload failed for user: {}", username);
               return ResponseEntity.status(400).body(message); // Bad request status for failed upload
           }
       } catch (Exception e) {
           logger.error("Error occurred while uploading image for user: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body("An error occurred while uploading the image. Please try again later.");
       }
   }

   @GetMapping("/images")
   public ResponseEntity<List<String>> getAllImagesForUser() {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body(null);
           }

           logger.debug("Fetching all images for user: {}", username);
           List<String> imageUrls = userService.getAllImagesForUser(username);

           if (imageUrls != null && !imageUrls.isEmpty()) {
               logger.info("Images fetched successfully for user: {}", username);
               return ResponseEntity.ok(imageUrls);
           } else {
               logger.warn("No images found for user: {}", username);
               return ResponseEntity.status(204).body(null); // No Content status if no images found
           }
       } catch (Exception e) {
           logger.error("Error occurred while fetching images for user: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body(null);
       }
   } 
   
   @DeleteMapping("/delete-image")
   public ResponseEntity<String> deleteImage(@RequestParam("imageUrl") String imageUrl) {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body("User not authenticated");
           }

           logger.debug("Attempting to delete image for user: {}", username);
           String message = userService.deleteImage(username, imageUrl);

           if ("Image deleted successfully".equals(message)) {
               logger.info("Image deleted successfully for user: {}", username);
               return ResponseEntity.ok(message);
           } else {
               logger.warn("Image deletion failed for user: {}", username);
               return ResponseEntity.status(400).body(message); // Bad request status for failed deletion
           }
       } catch (Exception e) {
           logger.error("Error occurred while deleting image for user: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body("An error occurred while deleting the image. Please try again later.");
       }
   }

   @GetMapping("/profile-with-images")
   public ResponseEntity<Map<String, Object>> getUserProfileWithImages() {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body(null);
           }

           logger.debug("Fetching user profile for username: {}", username);
           User user = userService.getUserProfile(username);
           if (user == null) {
               logger.warn("User not found: {}", username);
               return ResponseEntity.status(404).body(null);
           }

           logger.debug("Fetching images for username: {}", username);
           List<String> imageUrls = userService.getAllImagesForUser(username);

           Map<String, Object> response = new HashMap<>();
           response.put("user", user);
           response.put("images", imageUrls);

           logger.info("User profile and images fetched successfully for username: {}", username);
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           logger.error("Error occurred while fetching user profile and images for username: {}", SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body(null);
       }
   }

   @GetMapping("/image/{imageId}")
   public ResponseEntity<String> getImage(@PathVariable String imageId) {
	   try {
           String username = SecurityContextHolder.getContext().getAuthentication().getName();
           if (username == null) {
               logger.warn("User not authenticated");
               return ResponseEntity.status(401).body("User not authenticated");
           }

           logger.debug("Fetching image with imageId: {} for user: {}", imageId, username);
           String imageUrl = userService.getImage(imageId);

           if (imageUrl != null) {
               logger.info("Image fetched successfully for user: {}", username);
               return ResponseEntity.ok(imageUrl);
           } else {
               logger.warn("Image not found for imageId: {} for user: {}", imageId, username);
               return ResponseEntity.status(404).body("Image not found");
           }
       } catch (Exception e) {
           logger.error("Error occurred while fetching image with imageId: {} for user: {}", imageId, SecurityContextHolder.getContext().getAuthentication().getName(), e);
           return ResponseEntity.status(500).body("An error occurred while fetching the image. Please try again later.");
       }
   }
}
