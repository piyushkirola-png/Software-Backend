package com.softwareuniverse.repository;

import com.softwareuniverse.entity.WebhookInboundLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookInboundLogRepository
  extends JpaRepository<WebhookInboundLog, Long>
{
  List<WebhookInboundLog> findTop50ByOrderByReceivedAtDesc();

  List<WebhookInboundLog> findByGatewayOrderByReceivedAtDesc(String gateway);
}
