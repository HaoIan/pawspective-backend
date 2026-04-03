package com.fyp.pawspective.service;

import com.fyp.pawspective.io.EditProfileRequest;
import com.fyp.pawspective.io.ProfileRequest;
import com.fyp.pawspective.io.ProfileResponse;

import java.util.List;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    ProfileResponse updateProfile(String email, EditProfileRequest request);

    void deleteProfileByEmail(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    void resetOtp(String email, String otp);

    List<ProfileResponse> getAllProfiles();

}