package com.swd392.services.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface CloudinaryService {

  /**
   * Upload a single file (image or pdf) to Cloudinary
   *
   * @param file the file to upload
   * @return URL of the uploaded file
   */
  String uploadFileImage(MultipartFile file) throws IOException;

  /**
   * Upload multiple files to Cloudinary
   *
   * @param files list of files to upload
   * @return List of URLs of the uploaded files
   */
  List<String> uploadMultipleFilesImages(List<MultipartFile> files) throws IOException;

  /**
   * Delete a file from Cloudinary based on its URL
   *
   * @param url the exact URL returned when uploading the file
   */
  void deleteFile(String url) throws IOException;
}
