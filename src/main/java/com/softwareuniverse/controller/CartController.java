package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.AddToCartRequest;
import com.softwareuniverse.dto.response.CartResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.CartService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getCart(userId)));
  }

  @GetMapping("/count")
  public ResponseEntity<ApiResponse<Long>> getCartCount(Principal principal) {
    if (principal == null) {
      return ResponseEntity.ok(ApiResponse.success("Cart count", 0L));
    }
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(ApiResponse.success("Cart count", cartService.getCartCount(userId)));
  }

  @PostMapping("/add")
  public ResponseEntity<ApiResponse<CartResponse>> addToCart(
      Principal principal, @Valid @RequestBody AddToCartRequest request) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(ApiResponse.success("Added to cart", cartService.addToCart(userId, request)));
  }

  @PutMapping("/items/{cartItemId}")
  public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
      Principal principal,
      @PathVariable Long cartItemId,
      @RequestParam Integer quantity) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success("Cart updated", cartService.updateQuantity(userId, cartItemId, quantity)));
  }

  @DeleteMapping("/items/{cartItemId}")
  public ResponseEntity<ApiResponse<CartResponse>> removeItem(
      Principal principal, @PathVariable Long cartItemId) {
    Long userId = currentUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success("Item removed", cartService.removeItem(userId, cartItemId)));
  }

  @DeleteMapping("/clear")
  public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
    Long userId = currentUserId(principal);
    cartService.clearCart(userId);
    return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) {
      throw new ResourceNotFoundException("Unauthorized — please login");
    }
    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}