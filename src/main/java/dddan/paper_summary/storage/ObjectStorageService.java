package dddan.paper_summary.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final S3Client s3Client;

    @Value("${ncp.s3.bucket}")
    private String bucketName;

    @Value("${ncp.s3.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }

        // 확장자 기반 폴더 결정
        String folder = getFolderByExtension(originalFileName);
        String key = folder + "/" + originalFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build(); // serverSideEncryption 생략 가능

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String host = URI.create(endpoint).getHost();
            return String.format("https://%s.%s/%s", bucketName, host, key);

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 확장자에 따라 폴더 분기
    private String getFolderByExtension(String filename) {
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif") || lowerName.endsWith(".bmp")) {
            return "images";
        } else if (lowerName.endsWith(".md") || lowerName.endsWith(".markdown")) {
            return "markdown";
        } else if (lowerName.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerName.endsWith(".csv")) {
            return "csv";
        } else {
            return "etc"; // 예외 확장자 처리 (선택)
        }
    }
}
