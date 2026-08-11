package com.learningsp.controller;

import com.learningsp.dto.common.ApiResponse;
import com.learningsp.dto.report.DashboardResponse;
import com.learningsp.dto.report.ReportResponse;
import com.learningsp.service.ReportService;
import com.learningsp.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboard(principal.getUserId())));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reports(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReport(principal.getUserId())));
    }
}
