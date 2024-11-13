package com.example.Test.Service;

import com.example.Test.Models.AppUser;
import com.example.Test.Repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final AppUserRepository appUserRepository;

    // Constructor-based dependency injection
    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Save a new user (no password encoding as requested)
    public void saveUser(AppUser appUser) {
        appUserRepository.save(appUser);
    }

    // Find a user by email
    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public AppUser findByResetToken(String resetToken) {
        return appUserRepository.findByResetToken(resetToken);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user from the repository using the email
        AppUser appUser = appUserRepository.findByEmail(email);

        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Return a UserDetails object with the user's data
        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())  // Raw password (or encoded if you apply encoding)
                .build();
    }

}
