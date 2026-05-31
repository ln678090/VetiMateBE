package com.graduation.project.common.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResp<T>(String message, T data, String timestamp) {
  public ApiResp {
    if (timestamp == null) {
      timestamp = java.time.Instant.now().toString();
    }
  }
}
