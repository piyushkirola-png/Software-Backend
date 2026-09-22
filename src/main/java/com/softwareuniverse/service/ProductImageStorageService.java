package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ProductImageStorageService {

  @Value("${app.uploads.dir:uploads}")
  private String uploadsDir;

  private static final String PRODUCT_SUBDIR = "products";
  private static final String CATEGORY_SUBDIR = "categories";

  private static final Set<String> ALLOWED_TYPES =
      Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
  private static final long MAX_SIZE = 5 * 1024 * 1024L; // 5 MB

  public String storeProduct(MultipartFile file) {
    return store(file, PRODUCT_SUBDIR);
  }

  public String storeCategory(MultipartFile file) {
    return store(file, CATEGORY_SUBDIR);
  }

  private String store(MultipartFile file, String subdir) {
    if (file == null || file.isEmpty()) {
      throw new ResourceNotFoundException("File is empty");
    }
    if (file.getSize() > MAX_SIZE) {
      throw new RuntimeException("File must be under 5 MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
      throw new RuntimeException("Only PNG, JPG, or WEBP images allowed");
    }

    try {
      Path dir = Paths.get(uploadsDir, subdir).toAbsolutePath().normalize();
      Files.createDirectories(dir);

      String ext = getExtension(file.getOriginalFilename(), contentType);
      String filename = UUID.randomUUID() + ext;
      Path target = dir.resolve(filename);

      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      log.info("Stored {} image: {}", subdir, target);

      return "/uploads/" + subdir + "/" + filename;
    } catch (IOException e) {
      log.error("Failed to store image", e);
      throw new RuntimeException("Failed to store image: " + e.getMessage());
    }
  }

  private String getExtension(String originalName, String contentType) {
    if (originalName != null) {
      int dot = originalName.lastIndexOf('.');
      if (dot >= 0) {
        String ext = originalName.substring(dot).toLowerCase();
        if (ext.matches("\\.(png|jpe?g|webp)")) return ext;
      }
    }
    return switch (contentType.toLowerCase()) {
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      default -> ".jpg";
    };
  }
}
