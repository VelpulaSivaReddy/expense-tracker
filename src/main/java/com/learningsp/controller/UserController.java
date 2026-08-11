package com.learningsp.controller;

import com.learningsp.dto.common.ApiResponse;
import com.learningsp.dto.user.UpdateProfileRequest;
import com.learningsp.dto.user.UserResponse;
import com.learningsp.exception.ResourceNotFoundException;
import com.learningsp.service.UserService;
import com.learningsp.util.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Path PROFILE_IMAGE_DIR = Paths.get("uploads/profile-images").normalize();

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getUserId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(principal.getUserId(), request)));
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> uploadImage(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Profile picture updated",
                userService.uploadProfileImage(principal.getUserId(), file)));
    }

    @GetMapping("/profile-image/{filename}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        // Filenames are server-generated (see UserService#uploadProfileImage), so reject anything
        // that isn't a bare filename to guard against path traversal via a crafted path variable.
        if (!StringUtils.hasText(filename) || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new ResourceNotFoundException("Image not found");
        }

        Path filePath = PROFILE_IMAGE_DIR.resolve(filename).normalize();
        if (!filePath.startsWith(PROFILE_IMAGE_DIR) || !filePath.toFile().exists()) {
            throw new ResourceNotFoundException("Image not found");
        }

        Resource resource = new FileSystemResource(filePath);
        MediaType contentType = resolveContentType(filename);
        return ResponseEntity.ok().contentType(contentType).body(resource);
    }

    private MediaType resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.valueOf("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}
