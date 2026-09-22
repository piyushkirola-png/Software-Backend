package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.service.ProductImageStorageService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUploadController {

  private final ProductImageStorageService storage;

  @PostMapping("/product")
  public ResponseEntity<ApiResponse<Map<String, String>>> uploadProduct(
      @RequestParam("file") MultipartFile file) {
    String url = storage.storeProduct(file);
    return ResponseEntity.ok(
        ApiResponse.success("Uploaded", Map.of("url", url)));
  }

  @PostMapping("/category")
  public ResponseEntity<ApiResponse<Map<String, String>>> uploadCategory(
      @RequestParam("file") MultipartFile file) {
    String url = storage.storeCategory(file);
    return ResponseEntity.ok(
        ApiResponse.success("Uploaded", Map.of("url", url)));
  }
}