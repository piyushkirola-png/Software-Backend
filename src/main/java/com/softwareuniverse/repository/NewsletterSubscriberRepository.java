package com.softwareuniverse.repository;

import com.softwareuniverse.entity.NewsletterSubscriber;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterSubscriberRepository
  extends JpaRepository<NewsletterSubscriber, Long>
{
  Optional<NewsletterSubscriber> findByEmail(String email);

  boolean existsByEmail(String email);
}
