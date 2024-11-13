package com.example.Test.Repository;

import com.example.Test.Models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByEmail(String email);  // Find user by email
    AppUser findByResetToken(String resetToken);  // Ensure this method is added
}
