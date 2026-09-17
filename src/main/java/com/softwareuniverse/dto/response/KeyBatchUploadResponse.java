package com.softwareuniverse.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyBatchUploadResponse {
  private int totalRows;
  private int inserted;
  private int skipped;   // duplicates or invalid rows
  private List<String> errors; // human-readable errors for skipped rows
}