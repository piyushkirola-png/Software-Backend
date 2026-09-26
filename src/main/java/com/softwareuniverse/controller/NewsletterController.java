package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.NewsletterRequest;
import com.softwareuniverse.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

  private final NewsletterService newsletterService;

  @PostMapping("/subscribe")
  public ResponseEntity<ApiResponse<Void>> subscribe(
    @Valid @RequestBody NewsletterRequest request
  ) {
    newsletterService.subscribe(request.getEmail());
    return ResponseEntity.ok(
      ApiResponse.success("Subscribed successfully", null)
    );
  }
}
