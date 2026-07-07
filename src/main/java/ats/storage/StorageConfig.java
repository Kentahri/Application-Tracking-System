package ats.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class StorageConfig {

    @Bean
    public MinioStorage minioStorage(FileStorageProperties props) {
        return new MinioStorage(props);
    }
}
