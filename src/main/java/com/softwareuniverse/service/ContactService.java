package com.softwareuniverse.service;

import com.softwareuniverse.dto.request.ContactRequest;

public interface ContactService {

  void submit(ContactRequest request);
}