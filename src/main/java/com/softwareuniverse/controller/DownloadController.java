package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.response.DownloadResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.DownloadService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {

  private final DownloadService downloadService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DownloadResponse>>> myDownloads(Principal principal) {
    return ResponseEntity.ok(
        ApiResponse.success("Downloads fetched", downloadService.getMyDownloads(currentUserId(principal))));
  }

  private Long currentUserId(Principal principal) {
    if (principal == null) throw new ResourceNotFoundException("Unauthorized");
    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return user.getId();
  }
}