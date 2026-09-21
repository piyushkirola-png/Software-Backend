package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySlug(String slug);

  boolean existsBySlug(String slug);

  List<Product> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();

  List<Product> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId);

  Page<Product> findByIsActiveTrue(Pageable pageable);

  Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

  @Query(
      "SELECT p FROM Product p WHERE p.isActive = true AND "
          + "(LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + " LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :q, '%')))")
  Page<Product> searchActive(@Param("q") String query, Pageable pageable);

  long countByIsActiveTrue();
}
