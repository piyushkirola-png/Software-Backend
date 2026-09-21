package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.AddressRequest;
import com.softwareuniverse.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {

  List<AddressResponse> getMyAddresses(Long userId);

  AddressResponse getAddress(Long userId, Long addressId);

  AddressResponse createAddress(Long userId, AddressRequest request);

  AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);

  void deleteAddress(Long userId, Long addressId);

  AddressResponse setDefault(Long userId, Long addressId);
}
