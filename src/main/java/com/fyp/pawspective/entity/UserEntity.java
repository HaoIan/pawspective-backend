package com.fyp.pawspective.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "USERS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_email", unique = true)
    private String email;

    @Column(name = "user_password")
    private String password;

    @Column(name = "user_phone")
    private String phone;

    @Column(name = "user_role")
    private String role;

    @Column(name = "user_verify_otp")
    private String verifyOtp;

    @Column(name = "user_is_account_verified")
    private Boolean isAccountVerified;

    @Column(name = "user_verify_otp_expire_at")
    private Long verifyOtpExpireAt;

    @Column(name = "user_reset_otp")
    private String resetOtp;

    @Column(name = "user_reset_otp_expire_at")
    private Long resetOtpExpireAt;

    @CreationTimestamp
    @Column(name = "user_created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "user_updated_at")
    private Timestamp updatedAt;
}
