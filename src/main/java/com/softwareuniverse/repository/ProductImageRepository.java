package com.softwareuniverse.repository;

import com.softwareuniverse.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

  List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
}