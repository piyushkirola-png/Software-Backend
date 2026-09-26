package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.KeyResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.KeyInventoryService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class KeyInventoryController {

  private final KeyInventoryService keyInventoryService;
  private final UserRepository userRepository;

  @GetMapping("/my")
  public ResponseEntity<ApiResponse<List<KeyResponse>>> myKeys(
    Principal principal
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success("Keys fetched", keyInventoryService.getMyKeys(userId))
    );
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse<List<KeyResponse>>> keysForOrder(
    Principal principal,
    @PathVariable Long orderId
  ) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Keys fetched",
        keyInventoryService.getKeysForOrder(userId, orderId)
      )
    );
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) {
      throw new ResourceNotFoundException("Unauthorized — please login");
    }
    User user = userRepository
      .findByEmail(principal.getName())
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}
