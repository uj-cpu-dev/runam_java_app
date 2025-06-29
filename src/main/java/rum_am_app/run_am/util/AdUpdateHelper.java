package rum_am_app.run_am.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.model.UserAd;

import java.util.*;

@Component
public class AdUpdateHelper {
    private final ImageUploader imageUploader;
    public AdUpdateHelper(ImageUploader imageUploader) {
        this.imageUploader = imageUploader;
    }

    public void updateBasicFields(UserAd existingAd, UserAd updatedAd) {
        existingAd.setTitle(updatedAd.getTitle());
        existingAd.setPrice(updatedAd.getPrice());
        existingAd.setCategory(updatedAd.getCategory());
        existingAd.setDescription(updatedAd.getDescription());
        existingAd.setLocation(updatedAd.getLocation());
        existingAd.setCondition(updatedAd.getCondition());
    }

    public Set<String> processImageUpdates(UserAd existingAd,
                                           UserAd updatedAd,
                                           List<MultipartFile> newImages,
                                           List<UserAd.ImageData> finalImages) {
        Set<String> imagesToDelete = new HashSet<>();

        // Process existing images
        if (updatedAd.getImages() != null) {
            processExistingImages(existingAd, updatedAd, finalImages, imagesToDelete);
            processBase64Images(updatedAd, existingAd.getUserId(), finalImages);
        } else {
            finalImages.addAll(existingAd.getImages());
        }

        // Process new file uploads
        if (newImages != null && !newImages.isEmpty()) {
            processFileUploads(newImages, existingAd.getUserId(), finalImages);
        }

        return imagesToDelete;
    }

    private void processExistingImages(UserAd existingAd,
                                       UserAd updatedAd,
                                       List<UserAd.ImageData> finalImages,
                                       Set<String> imagesToDelete) {
        for (UserAd.ImageData existingImage : existingAd.getImages()) {
            boolean imageKept = updatedAd.getImages().stream()
                    .anyMatch(updatedImage ->
                            (updatedImage.getId() != null && updatedImage.getId().equals(existingImage.getId())) ||
                                    (updatedImage.getUrl() != null && updatedImage.getUrl().equals(existingImage.getUrl())));

            if (imageKept) {
                finalImages.add(existingImage);
            } else {
                imagesToDelete.add(existingImage.getUrl());
            }
        }
    }

    private void processBase64Images(UserAd updatedAd,
                                     String userId,
                                     List<UserAd.ImageData> finalImages) {
        for (UserAd.ImageData updatedImage : updatedAd.getImages()) {
            if (updatedImage.getBase64Data() != null) {
                try {
                    MultipartFile file = imageUploader.convertBase64ToMultipartFile(
                            updatedImage.getBase64Data(),
                            updatedImage.getFilename() != null ?
                                    updatedImage.getFilename() : "image_" + System.currentTimeMillis() + ".png"
                    );

                    String url = imageUploader.uploadImageToS3(file, userId);
                    finalImages.add(createImageData(file, url));
                } catch (Exception e) {
                    // Continue with other images
                }
            }
        }
    }

    private void processFileUploads(List<MultipartFile> newImages,
                                    String userId,
                                    List<UserAd.ImageData> finalImages) {
        for (MultipartFile file : newImages) {
            try {
                String url = imageUploader.uploadImageToS3(file, userId);
                finalImages.add(createImageData(file, url));
            } catch (Exception e) {
                // Continue with other images
            }
        }
    }

    private UserAd.ImageData createImageData(MultipartFile file, String url) {
        UserAd.ImageData newImage = new UserAd.ImageData();
        newImage.setId(UUID.randomUUID().toString());
        newImage.setFilename(file.getOriginalFilename());
        newImage.setUrl(url);
        return newImage;
    }

    public void cleanupImages(Set<String> imagesToDelete) {
        imagesToDelete.forEach(imageUrl -> {
            try {
                imageUploader.deleteImageFromS3(imageUrl);
            } catch (Exception e) {
                // Logging handled in ImageUploader
            }
        });
    }
}
