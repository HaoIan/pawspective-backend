package com.fyp.pawspective.service;

import com.fyp.pawspective.entity.UserEntity;
import com.fyp.pawspective.entity.PostEntity;
import com.fyp.pawspective.io.EditProfileRequest;
import com.fyp.pawspective.io.ProfileRequest;
import com.fyp.pawspective.io.ProfileResponse;
import com.fyp.pawspective.repository.UserRepository;
import com.fyp.pawspective.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);

        if (!userRepository.existsByEmail(request.getEmail())) {
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, EditProfileRequest request) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean nameUpdated = false;
        boolean phoneUpdated = false;
        String oldName = existingUser.getName();
        String oldPhone = existingUser.getPhone();

        if (request.getName() != null) {
            nameUpdated = !request.getName().equals(oldName);
            existingUser.setName(request.getName());
        }
        if (request.getPhone() != null) {
            phoneUpdated = !request.getPhone().equals(oldPhone);
            existingUser.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            existingUser.setRole(request.getRole());
        }

        UserEntity updatedUser = userRepository.save(existingUser);

        // Update user's posts if name or phone was changed
        if (nameUpdated || phoneUpdated) {
            updateUserPosts(email, nameUpdated ? request.getName() : null, phoneUpdated ? request.getPhone() : null);
        }

        return convertToProfileResponse(updatedUser);
    }
    
    /**
     * Updates the name and/or phone number in all posts owned by a user
     */
    private void updateUserPosts(String email, String newName, String newPhone) {
        // Fetch all posts by the user (in batches to handle large numbers efficiently)
        int page = 0;
        int size = 100; // Process 100 posts at a time
        Pageable pageable = PageRequest.of(page, size);
        Page<PostEntity> postsPage;
        
        do {
            postsPage = postRepository.findByOwnerEmail(email, pageable);
            
            for (PostEntity post : postsPage.getContent()) {
                boolean updated = false;
                
                if (newName != null) {
                    post.setOwner(newName);
                    updated = true;
                }
                
                if (newPhone != null) {
                    post.setOwnerPhone(newPhone);
                    updated = true;
                }
                
                if (updated) {
                    postRepository.save(post);
                }
            }
            
            pageable = PageRequest.of(++page, size);
        } while (postsPage.hasNext());
    }

    @Override
    @Transactional
    public void deleteProfileByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email " + email + " not found");
        }
        userRepository.deleteByEmail(email);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        //Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        //Calculate expiry time (current time + 15 minutes in milliseconds)
        long otpExpiry = System.currentTimeMillis() + (15 * 60 * 1000);

        //Update the profile/user
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(otpExpiry);

        //Save in the database
        userRepository.save(existingEntity);

        try {
            //Send reset otp email
            emailService.sendResetPasswordOtpEmail(existingEntity.getEmail(), otp, existingEntity.getName());
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP has expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) {
            return;
        }

        //Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        //Calculate expiry time (current time + 15 mins in milliseconds)
        long otpExpiry = System.currentTimeMillis() + (15 * 60 * 1000);

        //Update the user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(otpExpiry);

        //Save in the database
        userRepository.save(existingUser);

        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp, existingUser.getName());
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP has expired");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void resetOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        // Check if the provided OTP matches the one stored in the database
        if (existingUser.getResetOtp() == null || !otp.equals(existingUser.getResetOtp())) {
            throw new IllegalArgumentException("Invalid OTP provided");
        }

        userRepository.save(existingUser);
    }

    @Override
    public List<ProfileResponse> getAllProfiles() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                   .map(user -> new ProfileResponse(
                       user.getUserId(),
                       user.getName(),
                       user.getEmail(),
                       user.getPhone(),
                       user.getRole(),
                       user.getIsAccountVerified()
                   ))
                   .collect(Collectors.toList());
    }


    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .phone(newProfile.getPhone())
                .role(newProfile.getRole())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
}