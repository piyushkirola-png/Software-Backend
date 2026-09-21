package com.softwareuniverse.repository;

import com.softwareuniverse.entity.ProductVariant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

  List<ProductVariant> findByProductIdAndIsActiveTrueOrderByDisplayOrderAsc(Long productId);
}
