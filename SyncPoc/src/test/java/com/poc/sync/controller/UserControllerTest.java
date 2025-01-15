package com.poc.sync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.sync.config.TestSecurityConfig;
import com.poc.sync.jwt.JwtUtil;
import com.poc.sync.model.LoginRequest;
import com.poc.sync.model.User;
import com.poc.sync.repository.UserRepository;
import com.poc.sync.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private UserService userService;
   
   @MockBean
   private UserRepository userRepository;

   @MockBean
   private JwtUtil jwtUtil;

   @Autowired
   private ObjectMapper objectMapper;

   private User user;

   @BeforeEach
   public void setUp() {
       user = new User();
       user.setUsername("testuser");
       user.setPassword("password");
       user.setEmail("testuser@example.com");
       user.setPhoneNumber("1234567890");
   }

   @Test
   public void testRegisterUser() throws Exception {
       when(userService.registerUser(anyString(), anyString(), anyString(), anyString())).thenReturn("User registered successfully");

       mockMvc.perform(post("/api/users/register")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(user)))
               .andExpect(status().isOk())
               .andExpect(content().string("User registered successfully"));

       verify(userService, times(1)).registerUser(anyString(), anyString(), anyString(), anyString());
   }

   @Test
   public void testLoginUser() throws Exception {
       when(userService.authenticateUser(anyString(), anyString())).thenReturn("Login successful");

       mockMvc.perform(post("/api/users/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(user)))
               .andExpect(status().isOk())
               .andExpect(content().string("Login successful"));

       verify(userService, times(1)).authenticateUser(anyString(), anyString());
   }
   
   @Test
   public void testGenerateToken() throws Exception {
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("password");

      User mockUser = new User();
      mockUser.setUsername("testuser");
      mockUser.setPassword(new BCryptPasswordEncoder().encode("password")); // Make sure the password is encoded

      when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
      when(jwtUtil.generateToken(anyString())).thenReturn("mockedToken");

      mockMvc.perform(post("/api/users/token")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(loginRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.token").value("mockedToken"));

      verify(userRepository, times(1)).findByUsername(anyString());
      verify(jwtUtil, times(1)).generateToken(anyString());
   }
   
   @Test
   @WithMockUser(username = "testuser")
   public void testUpdatePhoneNumber() throws Exception {
       when(userService.updatePhoneNumber(anyString(), anyString())).thenReturn("Phone number updated successfully");

       mockMvc.perform(put("/api/users/update-phone")
               .contentType(MediaType.APPLICATION_JSON)
               .content("9876543210"))
               .andExpect(status().isOk())
               .andExpect(content().string("Phone number updated successfully"));

       verify(userService, times(1)).updatePhoneNumber(anyString(), eq("9876543210"));
   }
   
   @Test
   @WithMockUser(username = "testuser")
   public void testUploadImage() throws Exception {
       MockMultipartFile file = new MockMultipartFile("image", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

       when(userService.uploadImage(anyString(), any(MultipartFile.class))).thenReturn("Image uploaded successfully");

       mockMvc.perform(multipart("/api/users/upload-image")
               .file(file))
               .andExpect(status().isOk())
               .andExpect(content().string("Image uploaded successfully"));

       verify(userService, times(1)).uploadImage(anyString(), any(MultipartFile.class));
   }
   
   @Test
   @WithMockUser(username = "testuser")
   public void testGetAllImagesForUser() throws Exception {
       List<String> imageUrls = Arrays.asList("http://example.com/image1.jpg", "http://example.com/image2.jpg");

       when(userService.getAllImagesForUser(anyString())).thenReturn(imageUrls);

       mockMvc.perform(get("/api/users/images")
               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0]").value("http://example.com/image1.jpg"))
               .andExpect(jsonPath("$[1]").value("http://example.com/image2.jpg"));

       verify(userService, times(1)).getAllImagesForUser(anyString());
   }
   
   @Test
   @WithMockUser(username = "testuser")
   public void testDeleteImage() throws Exception {
      String imageUrl = "http://example.com/image1.jpg";

      when(userService.deleteImage(anyString(), anyString())).thenReturn("Image deleted successfully");

      mockMvc.perform(delete("/api/users/delete-image")
              .param("imageUrl", imageUrl)
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(content().string("Image deleted successfully"));

      verify(userService, times(1)).deleteImage(anyString(), eq(imageUrl));
   }
   
   @Test
   @WithMockUser(username = "testuser")
   public void testGetUserProfileWithImages() throws Exception {
      User mockUser = new User();
      mockUser.setUsername("testuser");
      mockUser.setEmail("testuser@example.com");
      mockUser.setPhoneNumber("1234567890");

      List<String> imageUrls = Arrays.asList("http://example.com/image1.jpg", "http://example.com/image2.jpg");

      when(userService.getUserProfile(anyString())).thenReturn(mockUser);
      when(userService.getAllImagesForUser(anyString())).thenReturn(imageUrls);

      mockMvc.perform(get("/api/users/profile-with-images")
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.username").value("testuser"))
              .andExpect(jsonPath("$.user.email").value("testuser@example.com"))
              .andExpect(jsonPath("$.user.phoneNumber").value("1234567890"))
              .andExpect(jsonPath("$.images[0]").value("http://example.com/image1.jpg"))
              .andExpect(jsonPath("$.images[1]").value("http://example.com/image2.jpg"));

      verify(userService, times(1)).getUserProfile(anyString());
      verify(userService, times(1)).getAllImagesForUser(anyString());
   }

}
