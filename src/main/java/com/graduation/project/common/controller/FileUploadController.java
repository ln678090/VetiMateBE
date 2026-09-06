package com.graduation.project.common.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.graduation.project.common.resp.ApiResp;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

  private final Cloudinary cloudinary;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResp<Map<String, String>>> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folder", defaultValue = "petcare") String folder)
      throws IOException {

    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        cloudinary
            .uploader()
            .upload(file.getBytes(), ObjectUtils.asMap("folder", folder, "resource_type", "auto"));

    String url = (String) result.get("secure_url");
    String publicId = (String) result.get("public_id");

    return ResponseEntity.ok(
        ApiResp.<Map<String, String>>builder()
            .data(Map.of("url", url, "publicId", publicId))
            .message("Upload thành công")
            .build());
  }
}
