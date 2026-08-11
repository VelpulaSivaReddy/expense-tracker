package com.learningsp.controller;

import com.learningsp.service.ExportService;
import com.learningsp.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserPrincipal principal) {
        byte[] data = exportService.exportToCsv(principal.getUserId());
        return fileResponse(data, "expenses.csv", "text/csv");
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        byte[] data = exportService.exportToExcel(principal.getUserId());
        return fileResponse(data, "expenses.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal UserPrincipal principal) throws IOException {
        byte[] data = exportService.exportToPdf(principal.getUserId());
        return fileResponse(data, "expenses.pdf", "application/pdf");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] data, String filename, String contentType) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(data);
    }
}
