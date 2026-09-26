package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.entity.LicenseKey;
import java.util.List;

public interface KeyInventoryService {
  List<LicenseKey> findKeysByOrder(Long orderId);

  List<KeyResponse> getMyKeys(Long userId);

  List<KeyResponse> getKeysForOrder(Long userId, Long orderId);

  long countAvailable(Long productId, Long variantId);
}
