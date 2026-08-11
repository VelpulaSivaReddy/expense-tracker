package com.learningsp.service;

import com.learningsp.dto.user.UpdateProfileRequest;
import com.learningsp.dto.user.UserResponse;
import com.learningsp.entity.User;
import com.learningsp.exception.BadRequestException;
import com.learningsp.exception.ResourceNotFoundException;
import com.learningsp.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // In production, point this at object storage (S3, GCS, etc.) instead of local disk.
    private static final String UPLOAD_DIR = "uploads/profile-images";
    private static final long MAX_PROFILE_IMAGE_SIZE = 10L * 1024 * 1024; // 10MB, matches profile.html copy
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"));

    public UserResponse getProfile(Long userId) {
        return toResponse(getUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserOrThrow(userId);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = getUserOrThrow(userId);
        validateProfileImage(file);

        Path dir = Paths.get(UPLOAD_DIR);
        Files.createDirectories(dir);

        String extension = getFileExtension(file.getOriginalFilename());
        String filename = "user-" + userId + "-" + UUID.randomUUID() + "." + extension;
        Path target = dir.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        user.setProfileImage("/api/users/profile-image/" + filename);
        return toResponse(userRepository.save(user));
    }

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image to upload");
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new BadRequestException("Image must be 10MB or smaller");
        }

        String original = file.getOriginalFilename();
        String extension = getFileExtension(original);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, PNG, GIF, or WEBP images are allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPG, PNG, GIF, or WEBP images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .build();
    }
}
