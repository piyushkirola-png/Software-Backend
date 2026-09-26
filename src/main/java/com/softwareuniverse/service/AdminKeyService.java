package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.KeyUpdateRequest;
import com.softwareuniverse.dto.request.KeyUploadRequest;
import com.softwareuniverse.dto.response.KeyBatchUploadResponse;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.dto.response.KeyStockResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface AdminKeyService {
  KeyResponse addKey(KeyUploadRequest request);

  void syncStock(Long productId, Long variantId);

  KeyResponse updateKey(Long id, KeyUpdateRequest request);

  KeyBatchUploadResponse bulkUploadCsv(
    MultipartFile file,
    Long productId,
    Long variantId,
    String batchName
  );

  Page<KeyResponse> getAllKeys(
    int page,
    int size,
    String status,
    Long productId,
    Long variantId,
    String search,
    String productSearch
  );

  List<KeyStockResponse> getStockSummary();

  KeyResponse revokeKey(Long id);

  void deleteKey(Long id);

  byte[] exportAvailableKeysCsv(Long productId, Long variantId);
}
