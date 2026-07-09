package ats.service.impl;

import ats.exception.BadRequestException;
import ats.service.CvTextExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
@Slf4j
public class CvTextExtractorServiceImpl implements CvTextExtractorService {

    @Override
    public String extract(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase(Locale.ROOT)
                : "";

        try {
            String text;
            if (fileName.endsWith(".pdf")) {
                text = extractPdf(file);
            } else if (fileName.endsWith(".docx")) {
                text = extractDocx(file);
            } else {
                log.warn("Unsupported CV file type: {}", file.getOriginalFilename());
                throw new BadRequestException("Only PDF and DOCX CV files are supported");
            }

            if (text.isBlank()) {
                log.warn("Extracted blank text from CV file: {}", file.getOriginalFilename());
                throw new BadRequestException("Cannot extract text from CV file");
            }
            return text;
        } catch (IOException e) {
            log.warn("Failed to extract text from CV file: {}", file.getOriginalFilename(), e);
            throw new BadRequestException("Cannot read text from CV file");
        }
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (var document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return normalize(stripper.getText(document));
        }
    }

    private String extractDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder builder = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    builder.append(text).append('\n');
                }
            });
            return normalize(builder.toString());
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\u0000", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
