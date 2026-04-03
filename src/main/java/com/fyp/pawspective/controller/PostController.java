package com.fyp.pawspective.controller;

import com.fyp.pawspective.entity.PostEntity;
import com.fyp.pawspective.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fyp.pawspective.constant.Constant.PHOTO_DIRECTORY;
import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostEntity> createPost(@RequestBody PostEntity postEntity) {
        return ResponseEntity.created(URI.create("/posts/postID")).body(postService.CreatePost(postEntity));
    }

    @GetMapping
    public ResponseEntity<Page<PostEntity>> getPosts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "12") int size) {
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostEntity> getPost(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(postService.getPost(id));
    }

    @GetMapping(path = "/image/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE })
    public byte[] getImage(@PathVariable("filename") String filename) throws Exception {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostEntity>> getPostsByCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok().body(postService.getPostsByCurrentUser(page, size));
    }

    @PutMapping("/photo")
    public ResponseEntity<String> uploadImage(@RequestParam("id") String id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok().body(postService.uploadImage(id, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostEntity> updatePost(
            @PathVariable(value = "id") String id,
            @RequestBody PostEntity postUpdateDetails) {
        PostEntity updatedPost = postService.updatePost(id, postUpdateDetails);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(value = "id") String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}