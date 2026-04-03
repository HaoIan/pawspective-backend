package com.fyp.pawspective.controller;

import com.fyp.pawspective.io.EditProfileRequest;
import com.fyp.pawspective.io.ProfileRequest;
import com.fyp.pawspective.io.ProfileResponse;
import com.fyp.pawspective.service.EmailService;
import com.fyp.pawspective.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(request);
        emailService.sendWelcomeEmail(response.getEmail(), response.getName());

        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return profileService.getProfile(email);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(
        @CurrentSecurityContext(expression = "authentication?.name") String email,
        @Valid @RequestBody EditProfileRequest request) {
        return profileService.updateProfile(email, request);
    }

    @PutMapping("/profile/{email}")
    @PreAuthorize("hasRole('ADMIN')")  // Ensure only admins can access this endpoint
    public ProfileResponse adminUpdateUserProfile(
            @PathVariable String email,
            @Valid @RequestBody EditProfileRequest request) {
        return profileService.updateProfile(email, request);
    }

    @GetMapping("/users")
    public List<ProfileResponse> getAllUsers() {
        return profileService.getAllProfiles();
    }

    @DeleteMapping("/users/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String email) {
        profileService.deleteProfileByEmail(email);
    }
}