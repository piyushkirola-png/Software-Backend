package com.softwareuniverse.repository;

import com.softwareuniverse.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  List<CartItem> findByCartId(Long cartId);

  Optional<CartItem> findByCartIdAndProductIdAndVariantId(
      Long cartId, Long productId, Long variantId);

  Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

  void deleteByCartId(Long cartId);

  long countByCartId(Long cartId);
}