package com.quodex._miles.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.quodex._miles.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    // Injected Cloudinary instance
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:28miles}")
    private String uploadFolder;

    /**
     * Uploads a file to Cloudinary
     * @param file Multipart file to upload
     * @return Public URL of the uploaded image
     */
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // Generate a unique file name using UUID
            String fileName = UUID.randomUUID().toString();

            // Upload the file to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", uploadFolder + "/" + fileName,
                            "resource_type", "image"
                    )
            );

            // Return the secure URL from Cloudinary
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary upload failed", e);
        }
    }

    /**
     * Deletes a file from Cloudinary
     * @param imgUrl Public URL of the image to delete
     * @return true if deletion is assumed successful
     */
    @Override
    public boolean deleteFile(String imgUrl) {
        try {
            if (imgUrl == null || imgUrl.isBlank()) {
                System.out.println("No image URL provided, skipping Cloudinary deletion.");
                return true;
            }

            // Extract the public ID from the URL
            String publicId = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
            if (publicId.contains(".")) {
                publicId = publicId.substring(0, publicId.lastIndexOf("."));
            }

            publicId = uploadFolder + "/" + publicId;

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;

        } catch (Exception e) {
            System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
            // You may want to log this or handle it differently
            return false;
        }
    }

}
