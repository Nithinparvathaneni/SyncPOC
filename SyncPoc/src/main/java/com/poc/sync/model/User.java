package com.poc.sync.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
@Table(name="\"user\"")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column(unique = true, nullable = false)
   private String username;
   @Column(nullable = false)
   private String password;
   @Column(nullable = false)
   private String email;
   @Column(nullable = false)
   private String phoneNumber;
   @Column(nullable = false)
   private String role = "USER";
   @ElementCollection
   private List<String> imageUrls = new ArrayList<>();

   public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
   public List<String> getImageUrls() {
	return imageUrls;
}
public void setImageUrls(List<String> imageUrls) {
	this.imageUrls = imageUrls;
}
public String getUsername() {
	return username;
}
public void setUsername(String username) {
	this.username = username;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getRole() {
	return role;
}
public void setRole(String role) {
	this.role = role;
}
public String getPhoneNumber() {
	return phoneNumber;
}
public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
}
}