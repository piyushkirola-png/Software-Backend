package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.KeyUploadRequest;
import com.softwareuniverse.dto.response.KeyBatchUploadResponse;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.dto.response.KeyStockResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface AdminKeyService {

  /** Manually add a single key. */
  KeyResponse addKey(KeyUploadRequest request);

  /** Bulk upload via CSV. Format: license_key[,notes] — one per line. */
  KeyBatchUploadResponse bulkUploadCsv(
      MultipartFile file, Long productId, Long variantId, String batchName);

  /** All keys (paginated), with optional filters. */
  Page<KeyResponse> getAllKeys(int page, int size, String status, Long productId, Long variantId);

  /** Stock summary across all products/variants. */
  List<KeyStockResponse> getStockSummary();

  /** Revoke a key (invalid / blacklisted). */
  KeyResponse revokeKey(Long id);

  /** Delete a key permanently (only AVAILABLE keys). */
  void deleteKey(Long id);

  /** Download a CSV of all AVAILABLE keys for a product/variant — optional, may skip. */
  byte[] exportAvailableKeysCsv(Long productId, Long variantId);
}
