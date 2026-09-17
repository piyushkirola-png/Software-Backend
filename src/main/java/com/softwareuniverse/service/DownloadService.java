package com.softwareuniverse.service;

import com.softwareuniverse.dto.response.DownloadResponse;
import java.util.List;

public interface DownloadService {

  List<DownloadResponse> getMyDownloads(Long userId);
}