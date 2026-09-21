package com.softwareuniverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoftwareUniverseApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoftwareUniverseApplication.class, args);
  }
}
