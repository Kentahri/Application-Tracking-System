package ats.service;

import org.springframework.web.multipart.MultipartFile;

public interface CvTextExtractorService {

    String extract(MultipartFile file);
}
