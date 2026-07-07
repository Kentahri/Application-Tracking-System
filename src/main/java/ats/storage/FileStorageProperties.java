package ats.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "file.cv")
public class FileStorageProperties {

    private String endpoint;

    private String accessKey = "";

    private String secretKey = "";

    private String bucket;

    private String basePath = "cvs";

    private boolean createBucket = true;

    private Duration presignExpiry = Duration.ofMinutes(15);

    private Timeouts timeouts = new Timeouts();

    @Data
    public static class Timeouts {
        private Duration connect = Duration.ofSeconds(5);
        private Duration read = Duration.ofSeconds(30);
        private Duration write = Duration.ofSeconds(30);
        private Duration call = Duration.ofSeconds(60);
    }
}
