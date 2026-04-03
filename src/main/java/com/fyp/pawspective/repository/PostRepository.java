package com.fyp.pawspective.repository;

import com.fyp.pawspective.entity.PostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String> {
    Optional<PostEntity> findById(String id);

    Page<PostEntity> findByOwnerEmail(String ownerEmail, Pageable pageable);

    Page<PostEntity> findByOwner(String userEmail, PageRequest date);
}