package com.learningsp.config;

import com.learningsp.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener {

    private final FileUploadService fileUploadService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStart() {
        log.info("Application started. Initializing upload directories...");
        fileUploadService.ensureUploadDirectoryExists();
        log.info("Application initialization complete.");
    }
}
