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

  List<Product> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(
    Long categoryId
  );

  Page<Product> findByIsActiveTrue(Pageable pageable);

  Page<Product> findByCategoryIdAndIsActiveTrue(
    Long categoryId,
    Pageable pageable
  );

  @Query(
    "SELECT p FROM Product p WHERE p.isActive = true AND " +
      "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR p.price <= :maxPrice)"
  )
  Page<Product> findActiveByPriceRange(
    @Param("minPrice") java.math.BigDecimal minPrice,
    @Param("maxPrice") java.math.BigDecimal maxPrice,
    Pageable pageable
  );

  @Query(
    "SELECT p FROM Product p WHERE p.isActive = true AND " +
      "p.category.id = :categoryId AND " +
      "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR p.price <= :maxPrice)"
  )
  Page<Product> findActiveByCategoryAndPriceRange(
    @Param("categoryId") Long categoryId,
    @Param("minPrice") java.math.BigDecimal minPrice,
    @Param("maxPrice") java.math.BigDecimal maxPrice,
    Pageable pageable
  );

  @Query(
    "SELECT p FROM Product p WHERE p.isActive = true AND " +
      "(LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
      " LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :q, '%')))"
  )
  Page<Product> searchActive(@Param("q") String query, Pageable pageable);

  long countByIsActiveTrue();

  @Query(
    "SELECT p FROM Product p WHERE " +
      "(:name IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))) " +
      "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
      "AND (:statusActive IS NULL OR p.isActive = :statusActive) " +
      "AND (:licenseType IS NULL OR p.licenseType = :licenseType) " +
      "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR p.price <= :maxPrice)"
  )
  Page<Product> findAdminProducts(
    @Param("name") String name,
    @Param("categoryId") Long categoryId,
    @Param("statusActive") Boolean statusActive,
    @Param("licenseType") String licenseType,
    @Param("minPrice") java.math.BigDecimal minPrice,
    @Param("maxPrice") java.math.BigDecimal maxPrice,
    Pageable pageable
  );
}
