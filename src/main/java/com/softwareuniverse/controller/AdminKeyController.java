package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.KeyUploadRequest;
import com.softwareuniverse.dto.response.KeyBatchUploadResponse;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.dto.response.KeyStockResponse;
import com.softwareuniverse.service.AdminKeyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/keys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminKeyController {

  private final AdminKeyService adminKeyService;

  @PostMapping
  public ResponseEntity<ApiResponse<KeyResponse>> addKey(
      @Valid @RequestBody KeyUploadRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success("Key added", adminKeyService.addKey(request)));
  }

  @PostMapping("/bulk-upload")
  public ResponseEntity<ApiResponse<KeyBatchUploadResponse>> bulkUpload(
      @RequestParam("file") MultipartFile file,
      @RequestParam Long productId,
      @RequestParam(required = false) Long variantId,
      @RequestParam(required = false) String batchName) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Bulk upload complete",
            adminKeyService.bulkUploadCsv(file, productId, variantId, batchName)));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<KeyResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long variantId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Keys fetched",
            adminKeyService.getAllKeys(page, size, status, productId, variantId)));
  }

  @GetMapping("/stock-summary")
  public ResponseEntity<ApiResponse<List<KeyStockResponse>>> stockSummary() {
    return ResponseEntity.ok(
        ApiResponse.success("Stock summary", adminKeyService.getStockSummary()));
  }

  @PostMapping("/{id}/revoke")
  public ResponseEntity<ApiResponse<KeyResponse>> revoke(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Key revoked", adminKeyService.revokeKey(id)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    adminKeyService.deleteKey(id);
    return ResponseEntity.ok(ApiResponse.success("Key deleted", null));
  }

  @GetMapping("/export-csv")
  public ResponseEntity<byte[]> exportCsv(
      @RequestParam Long productId,
      @RequestParam(required = false) Long variantId) {
    byte[] data = adminKeyService.exportAvailableKeysCsv(productId, variantId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=keys.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(data);
  }
}