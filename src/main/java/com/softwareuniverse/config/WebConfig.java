package com.softwareuniverse.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.uploads.dir:uploads}")
  private String uploadsDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uploadsPath = Paths.get(uploadsDir).toAbsolutePath().toString();
    registry
      .addResourceHandler("/uploads/**")
      .addResourceLocations("file:" + uploadsPath + "/");

    String softwarePath = Paths.get("src/main/resources/software")
      .toAbsolutePath()
      .toString();
    registry
      .addResourceHandler("/software/**")
      .addResourceLocations("file:" + softwarePath + "/");

    String assetsPath = Paths.get("src/main/resources/assets")
      .toAbsolutePath()
      .toString();
    registry
      .addResourceHandler("/assets/**")
      .addResourceLocations("file:" + assetsPath + "/");
  }
}
