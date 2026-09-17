package com.softwareuniverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

  @NotBlank
  private String name;

  private String slug;
  private String description;
  private String imageUrl;
  private String iconUrl;
  private Integer displayOrder;
  private Boolean isActive;
}