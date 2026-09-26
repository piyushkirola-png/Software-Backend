package com.softwareuniverse.service;

import com.softwareuniverse.common.exception.ResourceNotFoundException;
import com.softwareuniverse.dto.request.AddressRequest;
import com.softwareuniverse.dto.response.AddressResponse;
import com.softwareuniverse.entity.Address;
import com.softwareuniverse.entity.User;
import com.softwareuniverse.repository.AddressRepository;
import com.softwareuniverse.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

  private final AddressRepository addressRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<AddressResponse> getMyAddresses(Long userId) {
    return addressRepository
      .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
      .stream()
      .map(this::toResponse)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public AddressResponse getAddress(Long userId, Long addressId) {
    Address a = addressRepository
      .findByIdAndUserId(addressId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    return toResponse(a);
  }

  @Override
  @Transactional
  public AddressResponse createAddress(Long userId, AddressRequest request) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Address address = new Address();
    address.setUser(user);
    applyRequest(address, request);

    // If this is the user's first address OR they set it as default → unset others
    boolean setDefault =
      Boolean.TRUE.equals(request.getIsDefault()) ||
      addressRepository
        .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
        .isEmpty();

    if (setDefault) {
      unsetDefaults(userId);
      address.setIsDefault(true);
    }

    addressRepository.save(address);
    return toResponse(address);
  }

  @Override
  @Transactional
  public AddressResponse updateAddress(
    Long userId,
    Long addressId,
    AddressRequest request
  ) {
    Address address = addressRepository
      .findByIdAndUserId(addressId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    applyRequest(address, request);

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      unsetDefaults(userId);
      address.setIsDefault(true);
    }

    addressRepository.save(address);
    return toResponse(address);
  }

  @Override
  @Transactional
  public void deleteAddress(Long userId, Long addressId) {
    Address address = addressRepository
      .findByIdAndUserId(addressId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    addressRepository.delete(address);
  }

  @Override
  @Transactional
  public AddressResponse setDefault(Long userId, Long addressId) {
    Address address = addressRepository
      .findByIdAndUserId(addressId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    unsetDefaults(userId);
    address.setIsDefault(true);
    addressRepository.save(address);
    return toResponse(address);
  }

  // ============ Helpers ============

  private void unsetDefaults(Long userId) {
    List<Address> existing =
      addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    for (Address a : existing) {
      if (Boolean.TRUE.equals(a.getIsDefault())) {
        a.setIsDefault(false);
        addressRepository.save(a);
      }
    }
  }

  private void applyRequest(Address a, AddressRequest r) {
    a.setFullName(r.getFullName());
    a.setPhone(r.getPhone());
    a.setAddressLine1(r.getAddressLine1());
    a.setAddressLine2(r.getAddressLine2());
    a.setCity(r.getCity());
    a.setState(r.getState());
    a.setPincode(r.getPincode());
    a.setCountry(r.getCountry() != null ? r.getCountry() : "India");
    a.setGstNumber(r.getGstNumber());
  }

  private AddressResponse toResponse(Address a) {
    return AddressResponse.builder()
      .id(a.getId())
      .fullName(a.getFullName())
      .phone(a.getPhone())
      .addressLine1(a.getAddressLine1())
      .addressLine2(a.getAddressLine2())
      .city(a.getCity())
      .state(a.getState())
      .pincode(a.getPincode())
      .country(a.getCountry())
      .isDefault(a.getIsDefault())
      .gstNumber(a.getGstNumber())
      .createdAt(a.getCreatedAt())
      .build();
  }
}
