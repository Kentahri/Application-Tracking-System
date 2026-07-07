package ats.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import okhttp3.OkHttpClient;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class MinioStorage {

    private final MinioClient client;
    private final FileStorageProperties props;

    public MinioStorage(FileStorageProperties props) {
        this.props = props;
        this.client = buildClient(props);
        if (props.isCreateBucket() && StringUtils.hasText(props.getBucket())) {
            ensureBucket(props.getBucket());
        }
    }

    /**
     * Download a file from {@code sourceUrl}, upload it to MinIO and return the stored key
     * along with original file name and content type.
     */
    public StoredResult uploadFromUrl(String sourceUrl, String fallbackName) {
        if (!StringUtils.hasText(sourceUrl)) {
            throw new IllegalArgumentException("sourceUrl must not be blank");
        }

        URI uri = URI.create(sourceUrl.trim());
        String sourceName = extractFileName(uri, fallbackName);
        byte[] data = download(uri);

        String contentType = detectContentType(uri, sourceName);
        String finalName = buildFinalFileName(sourceName);
        String key = buildKey(finalName);

        try (InputStream in = new ByteArrayInputStream(data)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(key)
                    .stream(in, data.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO putObject failed for key=" + key, e);
        }

        return new StoredResult(key, sourceName, contentType);
    }


    public StoredResult uploadFromMultipart(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile must not be empty");
        }

        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            original = (folder == null || folder.isBlank()) ? "cv" : folder;
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = guessFromExtension(original);
        }

        String finalName = buildFinalFileName(original);
        String key = buildKey(finalName);
        long size = file.getSize();

        try (InputStream in = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(key)
                    .stream(in, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO putObject failed for key=" + key, e);
        }

        return new StoredResult(key, original, contentType);
    }

    // region helpers

    private MinioClient buildClient(FileStorageProperties p) {
        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(p.getTimeouts().getConnect())
                .readTimeout(p.getTimeouts().getRead())
                .writeTimeout(p.getTimeouts().getWrite())
                .callTimeout(p.getTimeouts().getCall())
                .build();

        if (!StringUtils.hasText(p.getEndpoint())) {
            throw new IllegalStateException("file.cv.endpoint must be configured");
        }

        return MinioClient.builder()
                .httpClient(http)
                .endpoint(p.getEndpoint())
                .credentials(p.getAccessKey(), p.getSecretKey())
                .build();
    }

    private void ensureBucket(String bucket) {
        try {
            if (!client.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO ensureBucket failed: " + bucket, e);
        }
    }

    private byte[] download(URI uri) {
        HttpURLConnection conn = null;
        try {
            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout((int) props.getTimeouts().getConnect().toMillis());
            conn.setReadTimeout((int) props.getTimeouts().getRead().toMillis());
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("Failed to download CV from URL, HTTP " + code);
            }
            try (InputStream is = conn.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download CV from URL: " + uri, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String detectContentType(URI uri, String name) {
        try {
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("HEAD");
            int code = conn.getResponseCode();
            if (code >= 200 && code < 400) {
                String ct = conn.getContentType();
                if (StringUtils.hasText(ct)) {
                    return ct;
                }
            }
            conn.disconnect();
        } catch (Exception ignored) {
            // fall through to extension-based detection
        }
        return guessFromExtension(name);
    }

    private String guessFromExtension(String name) {
        if (name == null) return "application/octet-stream";
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "application/octet-stream";
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String extractFileName(URI uri, String fallback) {
        String path = uri.getPath();
        if (StringUtils.hasText(path)) {
            int slash = path.lastIndexOf('/');
            if (slash >= 0 && slash < path.length() - 1) {
                String last = path.substring(slash + 1);
                if (StringUtils.hasText(last)) {
                    return last;
                }
            }
        }
        return (fallback == null || fallback.isBlank()) ? "cv" : fallback;
    }

    private String buildKey(String finalName) {
        String base = props.getBasePath() == null ? "" : stripTrailingSlash(props.getBasePath());
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return base + "/" + date + "/" + finalName;
    }

    private String buildFinalFileName(String original) {
        String base = sanitizeBaseName(stripExtension(original).baseName);
        String ext = stripExtension(original).extension;
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuidStr = UUID.randomUUID().toString();
        String uuidLast = uuidStr.substring(uuidStr.lastIndexOf('-') + 1);
        String combined = base + "_" + uuidLast + "_" + ts;
        return ext == null ? combined : combined + "." + ext;
    }

    private NameParts stripExtension(String name) {
        if (name == null) return new NameParts("cv", null);
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) return new NameParts(name, null);
        return new NameParts(name.substring(0, dot), name.substring(dot + 1));
    }

    private String sanitizeBaseName(String base) {
        if (base == null || base.isBlank()) return "cv";
        return base.replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    // endregion

    public record StoredResult(String storedKey, String fileName, String contentType) {}

    private record NameParts(String baseName, String extension) {}
}
