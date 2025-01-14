package com.poc.sync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.poc.sync.model.User;
import com.poc.sync.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
   private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
   @Autowired
   private UserRepository userRepository;

   @Autowired
   private PasswordEncoder passwordEncoder;

   @Autowired
   private ImgurService imgurService;

   public String registerUser(String username, String password, String email, String phoneNumber) {
	   try {
           // Check if the username already exists
           Optional<User> existingUser = userRepository.findByUsername(username);
           if (existingUser.isPresent()) {
               logger.warn("Attempt to register with an existing username: {}", username);
               return "Username already exists";
           }

           User user = new User();
           user.setUsername(username);
           user.setPassword(passwordEncoder.encode(password));
           user.setEmail(email);
           user.setPhoneNumber(phoneNumber);

           userRepository.save(user);
           logger.info("User registered successfully: {}", username);
           return "User registered successfully";
       } catch (Exception e) {
           logger.error("Error occurred while registering user: {}", username, e);
           return "An error occurred during registration. Please try again later.";
       }
   }

   public String authenticateUser(String username, String password) {
	   try {
           logger.debug("Attempting to authenticate user: {}", username);

           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               User user = userOptional.get();
               if (passwordEncoder.matches(password, user.getPassword())) {
                   logger.info("User authenticated successfully: {}", username);
                   return "Login successful";
               } else {
                   logger.warn("Authentication failed for user: {}. Incorrect password.", username);
               }
           } else {
               logger.warn("Authentication failed for user: {}. Username not found.", username);
           }
       } catch (Exception e) {
           logger.error("Error occurred while authenticating user: {}", username, e);
           return "An error occurred during authentication. Please try again later.";
       }

       return "Invalid username or password";
   }

   public String updatePhoneNumber(String username, String phoneNumber) {
	   try {
           logger.debug("Attempting to update phone number for user: {}", username);

           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               User user = userOptional.get();
               user.setPhoneNumber(phoneNumber);
               userRepository.save(user);
               logger.info("Phone number updated successfully for user: {}", username);
               return "Phone number updated successfully";
           } else {
               logger.warn("User not found: {}", username);
               return "User not found";
           }
       } catch (Exception e) {
           logger.error("Error occurred while updating phone number for user: {}", username, e);
           return "An error occurred while updating the phone number. Please try again later.";
       }
   }

   public String uploadImage(String username, MultipartFile file) {
	   try {
           logger.debug("Attempting to upload image for user: {}", username);

           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               User user = userOptional.get();
               String imageUrl = imgurService.uploadImage(file);
               user.getImageUrls().add(imageUrl);
               userRepository.save(user);
               logger.info("Image uploaded successfully for user: {}", username);
               return "Image uploaded successfully";
           } else {
               logger.warn("User not found: {}", username);
               return "User not found";
           }
       } catch (Exception e) {
           logger.error("Error occurred while uploading image for user: {}", username, e);
           return "Failed to upload image. Please try again later.";
       }
   }
   
   public List<String> getAllImagesForUser(String username) {
	   try {
           logger.debug("Fetching all images for user: {}", username);

           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               User user = userOptional.get();
               logger.info("Images fetched successfully for user: {}", username);
               return user.getImageUrls();
           } else {
               logger.warn("User not found with username: {}", username);
               throw new UsernameNotFoundException("User not found with username: " + username);
           }
       } catch (UsernameNotFoundException e) {
           logger.error("User not found exception for username: {}", username, e);
           throw e; // Rethrow the exception to propagate it
       } catch (Exception e) {
           logger.error("Error occurred while fetching images for user: {}", username, e);
           throw new RuntimeException("Failed to fetch images. Please try again later.", e);
       }
   }
   
   public User getUserProfile(String username) {
	   try {
           logger.debug("Fetching user profile for username: {}", username);
           
           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               logger.info("User profile fetched successfully for username: {}", username);
               return userOptional.get();
           } else {
               logger.warn("User not found with username: {}", username);
               throw new UsernameNotFoundException("User not found with username: " + username);
           }
       } catch (UsernameNotFoundException e) {
           logger.error("User not found exception for username: {}", username, e);
           throw e; // Rethrow the exception to propagate it
       } catch (Exception e) {
           logger.error("Error occurred while fetching user profile for username: {}", username, e);
           throw new RuntimeException("Failed to fetch user profile. Please try again later.", e);
       }
   }

   public String deleteImage(String username, String imageUrl) {
	   try {
           logger.debug("Attempting to delete image for user: {}", username);

           Optional<User> userOptional = userRepository.findByUsername(username);
           if (userOptional.isPresent()) {
               User user = userOptional.get();
               if (user.getImageUrls().contains(imageUrl)) {
                   user.getImageUrls().remove(imageUrl);
                   userRepository.save(user);

                   // Assuming the deleteHash is part of the image URL, otherwise you need to store it separately
                   String deleteHash = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                   if (deleteHash.contains(".")) {
                       deleteHash = deleteHash.substring(0, deleteHash.lastIndexOf('.'));
                   }
                   imgurService.deleteImage(deleteHash);
                   logger.info("Image deleted successfully for user: {}", username);
                   return "Image deleted successfully";
               } else {
                   logger.warn("Image not found in user's profile: {}", imageUrl);
                   return "Image not found in user's profile";
               }
           } else {
               logger.warn("User not found: {}", username);
               return "User not found";
           }
       } catch (UsernameNotFoundException e) {
           logger.error("User not found exception for username: {}", username, e);
           throw e; // Rethrow the exception to propagate it
       } catch (Exception e) {
           logger.error("Error occurred while deleting image for user: {}", username, e);
           return "Failed to delete image. Please try again later.";
       }
   }
   
   public String getImage(String imageId) {
	   try {
           logger.debug("Fetching image with imageId: {}", imageId);
           String imageUrl = imgurService.getImage(imageId);
           logger.info("Image fetched successfully with imageId: {}", imageId);
           return imageUrl;
       } catch (Exception e) {
           logger.error("Error occurred while fetching image with imageId: {}", imageId, e);
           return "Failed to get image. Please try again later.";
       }
   }

   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	   try {
           logger.debug("Loading user by username: {}", username);

           Optional<com.poc.sync.model.User> userOptional = userRepository.findByUsername(username);
           if (!userOptional.isPresent()) {
               logger.warn("User not found with username: {}", username);
               throw new UsernameNotFoundException("User not found with username: " + username);
           }

           com.poc.sync.model.User user = userOptional.get();
           logger.info("User loaded successfully with username: {}", username);
           return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
       } catch (UsernameNotFoundException e) {
           logger.error("UsernameNotFoundException for username: {}", username, e);
           throw e; // Rethrow the exception to propagate it
       } catch (Exception e) {
           logger.error("Error occurred while loading user by username: {}", username, e);
           throw new RuntimeException("Failed to load user. Please try again later.", e);
       }
   }
}
