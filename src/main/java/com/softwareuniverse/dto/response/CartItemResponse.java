package com.softwareuniverse.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class CartItemResponse {

  private Long id;

  private Long productId;

  private String productTitle;

  private String productSlug;

  private String thumbnailUrl;

  private Long variantId;

  private String variantName;

  private Integer quantity;

  private BigDecimal unitPrice;

  private BigDecimal lineTotal;

}