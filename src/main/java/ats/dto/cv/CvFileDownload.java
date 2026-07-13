package ats.dto.cv;

import ats.storage.StoredFile;

import java.io.IOException;

public record CvFileDownload(
        StoredFile storedFile,
        String fileName
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        storedFile.close();
    }
}
