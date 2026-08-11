package com.learningsp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("${app.upload.directory:./uploads}")
    private String uploadDirectory;

    @Value("${app.upload.max-file-size:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif"));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/gif"));

    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        validateFile(file);
        
        // Create user directory
        Path userDir = Paths.get(uploadDirectory, "users", userId.toString());
        Files.createDirectories(userDir);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = "profile_" + UUID.randomUUID() + "." + extension;
        
        Path filePath = userDir.resolve(filename);
        
        // Save file
        file.transferTo(filePath.toFile());
        
        log.info("Profile image uploaded for user {}: {}", userId, filePath);
        
        // Return relative path for database storage
        return "users/" + userId + "/" + filename;
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        try {
            Path path = Paths.get(uploadDirectory, filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    public byte[] getFileContent(String filePath) throws IOException {
        Path path = Paths.get(uploadDirectory, filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        return Files.readAllBytes(path);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxFileSize + " bytes");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public void ensureUploadDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(uploadDirectory));
            log.info("Upload directory ensured: {}", uploadDirectory);
        } catch (IOException e) {
            log.error("Failed to create upload directory", e);
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }
}
