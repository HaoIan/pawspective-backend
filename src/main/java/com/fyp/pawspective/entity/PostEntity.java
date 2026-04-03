package com.fyp.pawspective.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "POSTS")
public class PostEntity {
    @Id
    @UuidGenerator
    @Column(name = "post_id", unique = true, updatable = false)
    private String id;

    @Column(name = "post_type")
    private String type;

    @Column(name = "post_title")
    private String title;

    @Column(name = "post_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "post_location")
    private String location;

    @Column(name = "post_breed")
    private String breed;

    @Column(name = "post_gender")
    private String gender;

    @Column(name = "post_age")
    private String age;

    @Column(name = "post_owner")
    private String owner;

    @Column(name = "post_owner_email")
    private String ownerEmail;

    @Column(name = "post_owner_phone")
    private String ownerPhone;

    @Column(name = "post_date")
    private Date date;

    @Column(name = "post_adopt_status")
    private String adoptStatus;

    @Column(name = "post_image_url")
    private String imageUrl;
}
