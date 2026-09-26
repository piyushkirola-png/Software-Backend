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
  private int skipped;
  private List<String> errors;
}
