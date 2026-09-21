package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.entity.LicenseKey;
import java.util.List;

public interface KeyInventoryService {

  /** Get all keys (with keys for the given product) — admin utility. */
  List<LicenseKey> findKeysByOrder(Long orderId);

  /** Get keys tied to a successful order for a specific user (user-side). */
  List<KeyResponse> getMyKeys(Long userId);

  /** Get keys for a specific order (user-side). */
  List<KeyResponse> getKeysForOrder(Long userId, Long orderId);

  /** Count available keys for a product (+ optional variant). */
  long countAvailable(Long productId, Long variantId);
}
