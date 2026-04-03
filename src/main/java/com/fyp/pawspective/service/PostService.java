package com.fyp.pawspective.service;

import com.fyp.pawspective.entity.PostEntity;
import com.fyp.pawspective.entity.UserEntity;
import com.fyp.pawspective.repository.PostRepository;
import com.fyp.pawspective.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.fyp.pawspective.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<PostEntity> getAllPosts(int page, int size) {
        return postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")));
    }

    public PostEntity getPost(String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public PostEntity CreatePost(PostEntity postEntity) {
        // Get the current user's email from authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // Look up the user by email to get their name
        UserEntity user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));

        postEntity.setType(postEntity.getType());

        // Set the owner to the user's name
        postEntity.setOwner(user.getName());
        postEntity.setOwnerEmail(user.getEmail());
        postEntity.setOwnerPhone(user.getPhone());
        postEntity.setDate(new java.util.Date());
        
        return postRepository.save(postEntity);
    }

    public String uploadImage(String id, MultipartFile file) {
        PostEntity postEntity = getPost(id);
        String imageUrl = imageFunction.apply(id, file);
        postEntity.setImageUrl(imageUrl);
        postRepository.save(postEntity);

        return imageUrl;
    }

    private final Function<String, String> fileExtension = fileName -> Optional.of(fileName)
            .filter(name -> name.contains("."))
            .map(name -> "." + name.substring(fileName.lastIndexOf(".") + 1)).orElse(".png");

    private final BiFunction<String, MultipartFile, String> imageFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());

        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();

            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/posts/image/" + id + fileExtension.apply(image.getOriginalFilename())).toUriString();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to save image");
        }
    };

    public Page<PostEntity> getPostsByCurrentUser(int page, int size) {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Return posts where the owner matches the current user's email
        Page<PostEntity> userPosts = postRepository.findByOwnerEmail(currentUserEmail,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")));

        return userPosts;
    }

    public PostEntity updatePost(String id, PostEntity postUpdateDetails) {
        // First check if the post exists
        PostEntity existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Check if the current user is the owner of the post
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Only allow updates if the current user is the owner of the post
        if (!existingPost.getOwnerEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to edit this post");
        }

        // Update the fields that are allowed to be edited
        existingPost.setType(postUpdateDetails.getType() != null ? postUpdateDetails.getType() : existingPost.getType());
        existingPost.setTitle(postUpdateDetails.getTitle());
        existingPost.setDescription(postUpdateDetails.getDescription());
        existingPost.setLocation(postUpdateDetails.getLocation());
        existingPost.setBreed(postUpdateDetails.getBreed());
        existingPost.setGender(postUpdateDetails.getGender());
        existingPost.setAge(postUpdateDetails.getAge());
        existingPost.setAdoptStatus(postUpdateDetails.getAdoptStatus());

        // Save the updated post
        return postRepository.save(existingPost);
    }

    public void deletePost(String id) {
        // First check if the post exists
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Check if the current user is the owner of the post
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Only allow deletion if the current user is the owner of the post
        if (!post.getOwnerEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        // If there's an image associated with the post, delete it from the file system
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            try {
                String filename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                Path imagePath = Paths.get(PHOTO_DIRECTORY + filename);
                Files.deleteIfExists(imagePath);
            } catch (Exception e) {
                log.warn("Failed to delete image file for post {}: {}", id, e.getMessage());
                // Continue with post deletion even if image deletion fails
            }
        }

        // Delete the post from the database
        postRepository.deleteById(id);
    }
}