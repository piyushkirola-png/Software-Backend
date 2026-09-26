package com.softwareuniverse.repository;

import com.softwareuniverse.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findBySlug(String slug);

  boolean existsByName(String name);

  boolean existsBySlug(String slug);

  List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

  Optional<Category> findBySlugIgnoreCase(String slug);

  List<Category> findBySlugContainingIgnoreCaseOrderBySlugAsc(String fragment);
}
