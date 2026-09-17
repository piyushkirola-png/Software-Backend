package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.AddToCartRequest;
import com.softwareuniverse.dto.response.CartResponse;

public interface CartService {

  CartResponse getCart(Long userId);

  CartResponse addToCart(Long userId, AddToCartRequest request);

  CartResponse updateQuantity(Long userId, Long cartItemId, Integer quantity);

  CartResponse removeItem(Long userId, Long cartItemId);

  void clearCart(Long userId);

  Long getCartCount(Long userId);
}