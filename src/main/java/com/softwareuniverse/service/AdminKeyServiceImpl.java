package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.KeyUploadRequest;
import com.softwareuniverse.dto.response.KeyBatchUploadResponse;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.dto.response.KeyStockResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminKeyServiceImpl implements AdminKeyService {

  private final LicenseKeyRepository licenseKeyRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;

  @Override
  @Transactional
  public KeyResponse addKey(KeyUploadRequest request) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    ProductVariant variant = null;
    if (Boolean.TRUE.equals(product.getHasVariants())) {
      if (request.getVariantId() == null) {
        throw new RuntimeException("This product requires a variant");
      }
      variant =
          variantRepository
              .findById(request.getVariantId())
              .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
      if (!variant.getProduct().getId().equals(product.getId())) {
        throw new RuntimeException("Variant does not belong to this product");
      }
    }

    LicenseKey key = new LicenseKey();
    key.setProduct(product);
    key.setVariant(variant);
    key.setLicenseKey(request.getLicenseKey().trim());
    key.setBatchName(request.getBatchName());
    key.setNotes(request.getNotes());
    key.setStatus(KeyStatus.AVAILABLE);

    licenseKeyRepository.save(key);
    syncStock(product, variant);
    return toResponse(key);
  }

  @Override
  @Transactional
  public KeyBatchUploadResponse bulkUploadCsv(
      MultipartFile file, Long productId, Long variantId, String batchName) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    ProductVariant variant = null;
    if (Boolean.TRUE.equals(product.getHasVariants())) {
      if (variantId == null) throw new RuntimeException("This product requires a variant");
      variant =
          variantRepository
              .findById(variantId)
              .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
      if (!variant.getProduct().getId().equals(product.getId())) {
        throw new RuntimeException("Variant does not belong to this product");
      }
    }

    int totalRows = 0;
    int inserted = 0;
    int skipped = 0;
    List<String> errors = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      int lineNumber = 0;
      List<LicenseKey> toSave = new ArrayList<>();
      Set<String> seenInFile = new HashSet<>();

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        line = line.trim();
        if (line.isEmpty()) continue;

        // Skip header row (first row, if it contains "key" or "license")
        if (lineNumber == 1
            && (line.toLowerCase().contains("license") || line.toLowerCase().contains("key"))) {
          continue;
        }

        totalRows++;

        // Format: license_key[,notes]
        String[] parts = line.split(",", 2);
        String licenseKey = parts[0].trim();
        String notes = parts.length > 1 ? parts[1].trim() : null;

        if (licenseKey.isEmpty()) {
          skipped++;
          errors.add("Line " + lineNumber + ": empty key");
          continue;
        }

        // In-file duplicate check
        if (!seenInFile.add(licenseKey)) {
          skipped++;
          errors.add("Line " + lineNumber + ": duplicate in file — " + mask(licenseKey));
          continue;
        }

        // DB duplicate check
        if (licenseKeyRepository.findByLicenseKey(licenseKey).isPresent()) {
          skipped++;
          errors.add("Line " + lineNumber + ": already exists in DB — " + mask(licenseKey));
          continue;
        }

        LicenseKey k = new LicenseKey();
        k.setProduct(product);
        k.setVariant(variant);
        k.setLicenseKey(licenseKey);
        k.setBatchName(batchName);
        k.setNotes(notes);
        k.setStatus(KeyStatus.AVAILABLE);

        toSave.add(k);
      }

      if (!toSave.isEmpty()) {
        licenseKeyRepository.saveAll(toSave);
        inserted = toSave.size();
      }

      syncStock(product, variant);
    } catch (Exception e) {
      log.error("Bulk upload failed: {}", e.getMessage(), e);
      throw new RuntimeException("CSV upload failed: " + e.getMessage());
    }

    return KeyBatchUploadResponse.builder()
        .totalRows(totalRows)
        .inserted(inserted)
        .skipped(skipped)
        .errors(errors)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<KeyResponse> getAllKeys(
      int page, int size, String status, Long productId, Long variantId, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    KeyStatus parsedStatus = null;
    if (status != null && !status.isBlank()) {
      try {
        parsedStatus = KeyStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException ignored) {
        // invalid status → treat as no filter
      }
    }
    final KeyStatus statusEnum = parsedStatus;

    String searchTerm = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;

    if (searchTerm != null) {
      Page<LicenseKey> resultPage =
          licenseKeyRepository.searchKeys(statusEnum, productId, variantId, searchTerm, pageable);
      return resultPage.map(this::toResponse);
    }

    // Fallback: existing in-memory behavior when no search term
    List<LicenseKey> all = licenseKeyRepository.findAll();

    List<LicenseKey> filtered =
        all.stream()
            .filter(k -> statusEnum == null || k.getStatus() == statusEnum)
            .filter(k -> productId == null || k.getProduct().getId().equals(productId))
            .filter(
                k ->
                    variantId == null
                        || (k.getVariant() != null && k.getVariant().getId().equals(variantId)))
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .toList();

    int start = Math.min(page * size, filtered.size());
    int end = Math.min(start + size, filtered.size());
    List<LicenseKey> pageContent = filtered.subList(start, end);

    return new PageImpl<>(
        pageContent.stream().map(this::toResponse).toList(), pageable, filtered.size());
  }

  @Override
  @Transactional(readOnly = true)
  public List<KeyStockResponse> getStockSummary() {
    List<Product> products = productRepository.findAll();
    List<KeyStockResponse> result = new ArrayList<>();

    for (Product p : products) {
      if (Boolean.TRUE.equals(p.getHasVariants())) {
        List<ProductVariant> variants =
            variantRepository.findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(p.getId());
        for (ProductVariant v : variants) {
          result.add(buildStock(p, v));
        }
      } else {
        result.add(buildStock(p, null));
      }
    }
    return result;
  }

  @Override
  @Transactional
  public KeyResponse revokeKey(Long id) {
    LicenseKey k =
        licenseKeyRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Key not found"));

    if (k.getStatus() == KeyStatus.SOLD) {
      throw new RuntimeException("Cannot revoke a SOLD key");
    }

    k.setStatus(KeyStatus.REVOKED);
    licenseKeyRepository.save(k);
    syncStock(k.getProduct(), k.getVariant());
    return toResponse(k);
  }

  @Override
  @Transactional
  public void deleteKey(Long id) {
    LicenseKey k =
        licenseKeyRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Key not found"));

    if (k.getStatus() == KeyStatus.SOLD) {
      throw new RuntimeException("Cannot delete a SOLD key");
    }
    licenseKeyRepository.delete(k);
    syncStock(k.getProduct(), k.getVariant());
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportAvailableKeysCsv(Long productId, Long variantId) {
    List<LicenseKey> keys =
        licenseKeyRepository.findByProductIdAndStatus(productId, KeyStatus.AVAILABLE);

    if (variantId != null) {
      keys =
          keys.stream()
              .filter(k -> k.getVariant() != null && k.getVariant().getId().equals(variantId))
              .toList();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("license_key,batch,uploaded_at\n");
    keys.forEach(
        k ->
            sb.append(k.getLicenseKey())
                .append(',')
                .append(k.getBatchName() != null ? k.getBatchName() : "")
                .append(',')
                .append(k.getUploadedAt())
                .append('\n'));

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  // ============ Helpers ============

  /** Sync product.stockQuantity = count of AVAILABLE keys. */
  private void syncStock(Product product, ProductVariant variant) {
    if (variant != null) {
      long count =
          licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
              product.getId(), variant.getId(), KeyStatus.AVAILABLE);
      variant.setStockQuantity((int) count);
      variantRepository.save(variant);
    } else {
      long count =
          licenseKeyRepository.countByProductIdAndStatus(product.getId(), KeyStatus.AVAILABLE);
      product.setStockQuantity((int) count);
      productRepository.save(product);
    }
  }

  private KeyStockResponse buildStock(Product p, ProductVariant v) {
    long available, reserved, sold, revoked;
    if (v != null) {
      available =
          licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
              p.getId(), v.getId(), KeyStatus.AVAILABLE);
      reserved =
          licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
              p.getId(), v.getId(), KeyStatus.RESERVED);
      sold =
          licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
              p.getId(), v.getId(), KeyStatus.SOLD);
      revoked =
          licenseKeyRepository.countByProductIdAndVariantIdAndStatus(
              p.getId(), v.getId(), KeyStatus.REVOKED);
    } else {
      available = licenseKeyRepository.countByProductIdAndStatus(p.getId(), KeyStatus.AVAILABLE);
      reserved = licenseKeyRepository.countByProductIdAndStatus(p.getId(), KeyStatus.RESERVED);
      sold = licenseKeyRepository.countByProductIdAndStatus(p.getId(), KeyStatus.SOLD);
      revoked = licenseKeyRepository.countByProductIdAndStatus(p.getId(), KeyStatus.REVOKED);
    }
    long total = available + reserved + sold + revoked;

    return KeyStockResponse.builder()
        .productId(p.getId())
        .productTitle(p.getTitle())
        .variantId(v != null ? v.getId() : null)
        .variantName(v != null ? v.getVariantName() : null)
        .available(available)
        .reserved(reserved)
        .sold(sold)
        .revoked(revoked)
        .total(total)
        .build();
  }

  private KeyResponse toResponse(LicenseKey k) {
    return KeyResponse.builder()
        .id(k.getId())
        .productId(k.getProduct().getId())
        .productTitle(k.getProduct().getTitle())
        .variantId(k.getVariant() != null ? k.getVariant().getId() : null)
        .variantName(k.getVariant() != null ? k.getVariant().getVariantName() : null)
        .licenseKey(k.getLicenseKey())
        .status(k.getStatus().name())
        .soldAt(k.getSoldAt())
        .build();
  }

  private String mask(String key) {
    if (key.length() <= 6) return "****";
    return key.substring(0, 4) + "****" + key.substring(key.length() - 2);
  }
}
