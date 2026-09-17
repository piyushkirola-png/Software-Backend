package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.AddToCartRequest;
import com.softwareuniverse.dto.response.CartItemResponse;
import com.softwareuniverse.dto.response.CartResponse;
import com.softwareuniverse.entity.*;
import com.softwareuniverse.repository.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CartResponse getCart(Long userId) {
    Cart cart = getOrCreateCart(userId);
    return toResponse(cart);
  }

  @Override
  @Transactional
  public CartResponse addToCart(Long userId, AddToCartRequest request) {
    Cart cart = getOrCreateCart(userId);

    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    if (!Boolean.TRUE.equals(product.getIsActive())) {
      throw new RuntimeException("Product is not available");
    }

    ProductVariant variant = null;
    BigDecimal unitPrice = product.getPrice();

    if (Boolean.TRUE.equals(product.getHasVariants())) {
      if (request.getVariantId() == null) {
        throw new RuntimeException("Please select a variant");
      }
      variant =
          variantRepository
              .findById(request.getVariantId())
              .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
      if (!variant.getProduct().getId().equals(product.getId())) {
        throw new RuntimeException("Variant does not belong to this product");
      }
      unitPrice = variant.getPrice();
    }

    int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

    // Check if item already exists
    CartItem existing = null;
    if (variant != null) {
      existing =
          cartItemRepository
              .findByCartIdAndProductIdAndVariantId(cart.getId(), product.getId(), variant.getId())
              .orElse(null);
    } else {
      existing =
          cartItemRepository
              .findByCartIdAndProductId(cart.getId(), product.getId())
              .orElse(null);
    }

    if (existing != null) {
      existing.setQuantity(existing.getQuantity() + quantity);
      cartItemRepository.save(existing);
    } else {
      CartItem item = new CartItem();
      item.setCart(cart);
      item.setProduct(product);
      item.setVariant(variant);
      item.setQuantity(quantity);
      item.setUnitPrice(unitPrice);
      cartItemRepository.save(item);
    }

    return toResponse(cart);
  }

  @Override
  @Transactional
  public CartResponse updateQuantity(Long userId, Long cartItemId, Integer quantity) {
    Cart cart = getOrCreateCart(userId);

    CartItem item =
        cartItemRepository
            .findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    if (!item.getCart().getId().equals(cart.getId())) {
      throw new RuntimeException("Unauthorized");
    }

    if (quantity == null || quantity < 1) {
      cartItemRepository.delete(item);
    } else {
      item.setQuantity(quantity);
      cartItemRepository.save(item);
    }

    return toResponse(cart);
  }

  @Override
  @Transactional
  public CartResponse removeItem(Long userId, Long cartItemId) {
    Cart cart = getOrCreateCart(userId);

    CartItem item =
        cartItemRepository
            .findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    if (!item.getCart().getId().equals(cart.getId())) {
      throw new RuntimeException("Unauthorized");
    }

    cartItemRepository.delete(item);
    return toResponse(cart);
  }

  @Override
  @Transactional
  public void clearCart(Long userId) {
    Cart cart = getOrCreateCart(userId);
    cartItemRepository.deleteByCartId(cart.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public Long getCartCount(Long userId) {
    return cartRepository.findByUserId(userId).map(c -> cartItemRepository.countByCartId(c.getId())).orElse(0L);
  }

  // ================= Helpers =================

  private Cart getOrCreateCart(Long userId) {
    return cartRepository
        .findByUserId(userId)
        .orElseGet(
            () -> {
              User user =
                  userRepository
                      .findById(userId)
                      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
              Cart cart = new Cart();
              cart.setUser(user);
              return cartRepository.save(cart);
            });
  }

  private CartResponse toResponse(Cart cart) {
    List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

    List<CartItemResponse> itemResponses =
        items.stream()
            .map(
                item ->
                    CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productTitle(item.getProduct().getTitle())
                        .productSlug(item.getProduct().getSlug())
                        .thumbnailUrl(item.getProduct().getThumbnailUrl())
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                        .variantName(item.getVariant() != null ? item.getVariant().getVariantName() : null)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
            .toList();

    BigDecimal subtotal =
        itemResponses.stream()
            .map(CartItemResponse::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalItems = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();

    return CartResponse.builder()
        .id(cart.getId())
        .items(itemResponses)
        .totalItems(totalItems)
        .subtotal(subtotal)
        .build();
  }
}