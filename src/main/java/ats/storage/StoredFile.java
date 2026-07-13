package ats.storage;

import java.io.InputStream;
import java.io.IOException;

public record StoredFile(
        InputStream inputStream,
        long size,
        String contentType,
        String objectKey
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
