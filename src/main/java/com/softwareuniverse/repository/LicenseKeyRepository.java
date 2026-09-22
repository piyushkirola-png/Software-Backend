package com.softwareuniverse.repository;

import com.softwareuniverse.entity.KeyStatus;
import com.softwareuniverse.entity.LicenseKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Long> {
  long countByProductIdAndStatus(Long productId, KeyStatus status);

  long countByProductIdAndVariantIdAndStatus(Long productId, Long variantId, KeyStatus status);

  List<LicenseKey> findByOrderId(Long orderId);

  Optional<LicenseKey> findByLicenseKey(String licenseKey);

  Optional<LicenseKey> findFirstByProductIdAndStatus(Long productId, KeyStatus status);

  Optional<LicenseKey> findFirstByProductIdAndVariantIdAndStatus(
      Long productId, Long variantId, KeyStatus status);

  List<LicenseKey> findByProductIdAndStatus(Long productId, KeyStatus status);

  List<LicenseKey> findByProductIdAndVariantIdAndStatus(
      Long productId, Long variantId, KeyStatus status);

  @Query(
      "SELECT k FROM LicenseKey k WHERE k.product.id = :productId "
          + "AND k.status = :status ORDER BY k.id ASC")
  List<LicenseKey> findAvailableByProduct(
      @Param("productId") Long productId, @Param("status") KeyStatus status);

  @Query(
      "SELECT k FROM LicenseKey k WHERE "
          + "(:status IS NULL OR k.status = :status) "
          + "AND (:productId IS NULL OR k.product.id = :productId) "
          + "AND (:variantId IS NULL OR k.variant.id = :variantId) "
          + "AND (:search IS NULL OR LOWER(k.licenseKey) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "ORDER BY k.id DESC")
  Page<LicenseKey> searchKeys(
      @Param("status") KeyStatus status,
      @Param("productId") Long productId,
      @Param("variantId") Long variantId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      "SELECT k FROM LicenseKey k WHERE k.product.id = :productId "
          + "AND k.variant.id = :variantId AND k.status = :status ORDER BY k.id ASC")
  List<LicenseKey> findAvailableByProductAndVariant(
      @Param("productId") Long productId,
      @Param("variantId") Long variantId,
      @Param("status") KeyStatus status);

  long countByStatus(KeyStatus status);
}
