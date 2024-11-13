package com.example.Test.Controller;

import com.example.Test.Models.AppUser;
import com.example.Test.Service.AppUserService;
import com.example.Test.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {

    private final AppUserService appUserService;
    private final EmailService emailService; // Service for sending emails

    @Autowired
    public UserController(AppUserService appUserService, EmailService emailService) {
        this.appUserService = appUserService;
        this.emailService = emailService;  // Initialize the email service here
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @GetMapping("/Forgotpassword")
    public String ShowForgotpassword() {
        return "Forgotpassword";
    }

    @GetMapping("/passwordresetform")
    public String Showpasswordresetform() {
        return "passwordresetform";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> login(
            @RequestParam("email") String email,
            @RequestParam("password") String password, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("email : "+email);
        AppUser user = appUserService.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);  // Store user in session
            response.put("status", "success");
            response.put("message", "Login successful!");
            response.put("redirectUrl", "/index");  // Include redirectUrl in the response

            System.out.println("Login Success Response: " + response); // Log the success response
        } else {
            response.put("status", "error");
            response.put("message", "Invalid email or password.");

            System.out.println("Login Error Response: " + response); // Log the error response
        }

        return response;  // Return JSON response to frontend
    }


    @PostMapping("/signup")
    @ResponseBody
    public Map<String, Object> signup(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("age") int age,
            @RequestParam("sex") String sex) {

        Map<String, Object> response = new HashMap<>();

        // Log incoming signup request
        System.out.println("Received signup request: " + email + ", " + username + ", " + age + ", " + sex);

        // Check if the email is already in use
        AppUser existingUser = appUserService.findByEmail(email);
        if (existingUser != null) {
            response.put("status", "error");
            response.put("message", "Email already in use. Please choose a different one.");
            return response;
        }

        try {
            // Create and save user
            AppUser newUser = new AppUser();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setAge(age);
            newUser.setSex(sex);

            appUserService.saveUser(newUser);

            response.put("status", "success");
            response.put("message", "Signup successful!");
            response.put("redirectUrl", "/login");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Signup failed. Please try again.");
            return response;
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Invalidate the session
        session.invalidate();

        response.put("status", "success");
        response.put("message", "Logout successful!");
        response.put("redirectUrl", "/login");  // Redirect to login after logout

        System.out.println("Logout Response: " + response); // Log the logout response

        return response;
    }

    // Handle password reset request
    @PostMapping("/api/passwordresetrequest")
    @ResponseBody
    public Map<String, Object> passwordResetRequest(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();

        // Find user by email
        AppUser user = appUserService.findByEmail(email);
        if (user == null) {
            response.put("status", "error");
            response.put("message", "Email not found.");
            return response;
        }

        // Generate a unique token for password reset
        String token = UUID.randomUUID().toString();
        user.setResetToken(token); // Store token in the user object
        appUserService.saveUser(user); // Save updated user with the token

        // Construct password reset link
        String resetLink = "http://localhost:8080/passwordresetform?token=" + token;
        try {
            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            response.put("status", "success");
            response.put("message", "Password reset link sent to your email.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send email. Please try again later.");
        }
        return response;
    }


    // Handle password reset form submission
    @PostMapping("/api/resetpassword")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam("token") String token,
                                             @RequestParam("password") String newPassword) {
        Map<String, Object> response = new HashMap<>();

        AppUser user = appUserService.findByResetToken(token);
        if (user == null) {
            response.put("status", "error");
            response.put("message", "Invalid or expired reset token.");
            return response;
        }

        // Update user's password
        user.setPassword(newPassword);
        user.setResetToken(null); // Clear the reset token
        appUserService.saveUser(user);

        response.put("status", "success");
        response.put("message", "Your password has been reset.");
        return response;
    }

}
