package com.softwareuniverse.service;

import com.softwareuniverse.entity.NewsletterSubscriber;
import com.softwareuniverse.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

  private final NewsletterSubscriberRepository repository;

  @Override
  @Transactional
  public void subscribe(String email) {
    if (repository.existsByEmail(email)) return;

    NewsletterSubscriber s = new NewsletterSubscriber();
    s.setEmail(email);
    s.setIsActive(true);
    repository.save(s);
  }
}