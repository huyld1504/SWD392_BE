package com.swd392.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.swd392.services.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

  private final Cloudinary cloudinary;

  @Override
  public String uploadFileImage(MultipartFile file) throws IOException {
    // Allows both images and PDFs (raw files) safely inside 'article_diagrams'
    // folder
    Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
        "folder", "article_diagrams",
        "resource_type", "auto"));
    return uploadResult.get("secure_url").toString();
  }

  @Override
  public List<String> uploadMultipleFilesImages(List<MultipartFile> files) throws IOException {
    List<String> urls = new ArrayList<>();
    if (files != null && !files.isEmpty()) {
      for (MultipartFile file : files) {
        if (file != null && !file.isEmpty()) {
          urls.add(uploadFileImage(file));
        }
      }
    }
    return urls;
  }

  @Override
  public void deleteFile(String url) throws IOException {
    String publicId = extractPublicId(url);
    if (publicId != null && !publicId.isEmpty()) {
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
  }

  private String extractPublicId(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    try {
      int uploadIndex = url.indexOf("/upload/");
      if (uploadIndex == -1) {
        return null;
      }
      // get string after /upload/
      String afterUpload = url.substring(uploadIndex + 8);
      // remove version like "v123456/" if exists
      int versionIndex = afterUpload.indexOf("/");
      if (versionIndex != -1) {
        String versionStr = afterUpload.substring(0, versionIndex);
        if (versionStr.matches("v\\d+")) {
          afterUpload = afterUpload.substring(versionIndex + 1);
        }
      }
      // remove extension
      int dotIndex = afterUpload.lastIndexOf(".");
      if (dotIndex != -1) {
        afterUpload = afterUpload.substring(0, dotIndex);
      }
      return afterUpload;
    } catch (Exception e) {
      return null;
    }
  }
}
