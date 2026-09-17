package com.softwareuniverse.controller;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.common.response.ApiResponse;
import com.softwareuniverse.dto.request.AddressRequest;
import com.softwareuniverse.dto.response.AddressResponse;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.UserRepository;
import com.softwareuniverse.service.AddressService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {

  private final AddressService addressService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<List<AddressResponse>>> list(Principal principal) {
    return ResponseEntity.ok(
        ApiResponse.success("Addresses fetched", addressService.getMyAddresses(currentUserId(principal))));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AddressResponse>> get(
      Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Address fetched", addressService.getAddress(currentUserId(principal), id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<AddressResponse>> create(
      Principal principal, @Valid @RequestBody AddressRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Address created", addressService.createAddress(currentUserId(principal), request)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<AddressResponse>> update(
      Principal principal, @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            "Address updated", addressService.updateAddress(currentUserId(principal), id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(Principal principal, @PathVariable Long id) {
    addressService.deleteAddress(currentUserId(principal), id);
    return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
  }

  @PostMapping("/{id}/default")
  public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
      Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("Default set", addressService.setDefault(currentUserId(principal), id)));
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