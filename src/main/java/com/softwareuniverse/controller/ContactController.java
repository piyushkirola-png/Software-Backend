package com.softwareuniverse.controller;

import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.ContactRequest;
import com.softwareuniverse.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> submit(
    @Valid @RequestBody ContactRequest request
  ) {
    contactService.submit(request);
    return ResponseEntity.ok(ApiResponse.success("Message sent", null));
  }
}
