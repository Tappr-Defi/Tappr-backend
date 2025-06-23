package com.semicolon.africa.tapprbackend.reciepts.services.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vomzersocials.user.data.models.Media;
import org.vomzersocials.user.data.repositories.MediaRepository;
import org.vomzersocials.user.dtos.requests.MediaRequest;
import org.vomzersocials.user.enums.MediaType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final MediaRepository mediaRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String cdnUrl;

    @Autowired
    public MediaServiceImpl(MediaRepository mediaRepository,
                            S3Client s3Client,
                            S3Presigner s3Presigner,
                            @Value("${vomzer.bucket-name}") String bucketName,
                            @Value("${vomzer.cdn-url}") String cdnUrl) {
        this.mediaRepository = mediaRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.cdnUrl = cdnUrl;
    }


    public Page<Media> searchForMedia(String search, MediaType mediaType, Pageable pageable) {
        if (search != null && mediaType != null) {
            return mediaRepository
                    .findByFilenameContainingIgnoreCaseAndMediaType(search, mediaType, pageable);
        } else if (search != null) {
            return mediaRepository
                    .findByFilenameContainingIgnoreCase(search, pageable);
        } else if (mediaType != null) {
            return mediaRepository
                    .findByMediaType(mediaType, pageable);
        } else {
            return mediaRepository.findAll(pageable);
        }
    }

    public Media uploadMedia(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        String folder;
        MediaType mediaType;

        mediaType = determineMediaType(contentType);
        folder = getFolderForMedia(mediaType);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File must have a valid name.");
        }
        String uniqueKey = folder + UUID.randomUUID() + "-" + originalFilename;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueKey)
                .contentType(contentType)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (S3Exception e) {
            logger.error("Failed to upload file to S3. Filename: {}. Error: {}", originalFilename, e.getMessage());
            throw new IOException("Failed to upload to S3: " + e.getMessage(), e);
        }

        Media media = new Media();
        media.setFilename(originalFilename);
        media.setMediaType(mediaType);
        media.setUrl(cdnUrl + "/" + uniqueKey);

        return mediaRepository.save(media);
    }


    public List<Media> getMediaByIds(List<UUID> mediaIds) {
        return mediaRepository.findAllById(mediaIds);
    }

    public Map<String, String> generatePreSignedUploadUrl(String originalFilename, String folder, String contentType) {
        if (contentType == null || !isValidContentType(contentType)) throw new IllegalArgumentException("Invalid content type: " + contentType);
        if (originalFilename == null || originalFilename.isBlank()) throw new IllegalArgumentException("File must have a valid name.");
        String key = (folder.endsWith("/") ? folder : folder + "/") + UUID.randomUUID() + "-" + originalFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest preSignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

        return Map.of("url", preSignedRequest.url().toString(), "key", key);
    }

    public void deleteMediaById(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + mediaId));

        String key = extractKeyFromUrl(media.getUrl());

        try {
            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(key)
                    .build()
            );
            mediaRepository.delete(media);
            logger.info("Successfully deleted media with ID {} and key {}", mediaId, key);
        } catch (S3Exception e) {
            logger.error("Failed to delete media from Walrus. ID: {}, Key: {}, Error: {}", mediaId, key, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to delete media from Walrus: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void downloadMediaById(UUID mediaId, OutputStream outputStream) throws IOException {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + mediaId));

        String key = extractKeyFromUrl(media.getUrl());
        logger.info("Starting download for media ID {} with key {}", mediaId, key);

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = s3Object.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            logger.info("Completed download for media ID {}", mediaId);
        } catch (S3Exception e) {
            logger.error("Download failed for media ID {}. Error: {}", mediaId, e.awsErrorDetails().errorMessage());
            throw new IOException("Failed to download media from Walrus: " + e.getMessage(), e);
        }
    }


    private String extractKeyFromUrl(String mediaUrl) {
        try {
            URI uri = new URI(mediaUrl);
            return uri.getPath().replaceFirst("^/+", "");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid media URL: " + mediaUrl, e);
        }
    }

    private String getFolderForMedia(MediaType mediaType) {
        switch (mediaType) {
            case VIDEO:
                return "videos/";
            case IMAGE:
                return "images/";
            case GIF:
                return "gifs/";
            case PDF:
                return "pdfs/";
            default:
                throw new IllegalArgumentException("Unsupported media type");
        }
    }

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "video/mp4",
            "application/pdf"
    );

    private boolean isValidContentType(String contentType) {
        return ALLOWED_CONTENT_TYPES.contains(contentType);
    }

//    private boolean isValidContentType(String contentType) {
//        return contentType.startsWith("image") || contentType.startsWith("video") ||
//                contentType.startsWith("gif") || contentType.startsWith("pdf");
//    }

    private MediaType determineMediaType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is missing.");
        }
        if (contentType.startsWith("video")) {
            return MediaType.VIDEO;
        } else if (contentType.startsWith("image")) {
            return MediaType.IMAGE;
        } else if (contentType.startsWith("gif")) {
            return MediaType.GIF;
        } else if (contentType.startsWith("pdf")) {
            return MediaType.PDF;
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + contentType);
        }
    }

    public Media saveMetadata(MediaRequest mediaRequest) {
        Media media = new Media();
        media.setFilename(mediaRequest.getFilename());
        media.setMediaType(mediaRequest.getMediaType());
        media.setUrl(cdnUrl + "/" + mediaRequest.getKey());

        return mediaRepository.save(media);
    }

    public Media deleteMediaByFilename(String filename) {
        Media media = (Media) mediaRepository.findByFilename(filename)
                .orElseThrow(() -> new RuntimeException("Media not found with filename: " + filename));

        String key = media.getMediaType().name().toLowerCase() + "/" + filename;

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        mediaRepository.delete(media);

        return media;
    }


}
