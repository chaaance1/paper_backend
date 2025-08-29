package dddan.paper_summary.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final S3Client s3Client;

    @Value("${ncp.s3.bucket}")
    private String bucketName;

    @Value("${ncp.s3.endpoint}") // 예: https://kr.object.ncloudstorage.com
    private String endpoint;

    /** MultipartFile 업로드 (컨트롤러에서 직접 호출) */
    public String upload(MultipartFile file, String key, boolean publicRead) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 업로드할 파일이 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        var put = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .acl(publicRead ? ObjectCannedACL.PUBLIC_READ : null)
                .build();

        s3Client.putObject(put, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return buildPublicUrl(key);
    }

    /** 기존: 로컬 파일(Path) 업로드 */
    public String uploadLocalFile(Path localPath, boolean publicRead) throws IOException {
        if (!Files.exists(localPath)) {
            throw new IllegalArgumentException("[ERROR] 파일이 존재하지 않습니다: " + localPath);
        }
        String filename = localPath.getFileName().toString();
        String folder = getFolderByExtension(filename);
        String key = folder + "/" + filename;
        return uploadLocalFile(localPath, key, publicRead);
    }

    /** 기존: 로컬 파일(Path) 업로드 - 키 지정 */
    public String uploadLocalFile(Path localPath, String key, boolean publicRead) throws IOException {
        if (!Files.exists(localPath)) {
            throw new IllegalArgumentException("[ERROR] 파일이 존재하지 않습니다: " + localPath);
        }

        String contentType = Files.probeContentType(localPath);
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        var put = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .acl(publicRead ? ObjectCannedACL.PUBLIC_READ : null)
                .build();

        s3Client.putObject(put, RequestBody.fromFile(localPath));
        return buildPublicUrl(key);
    }

    /** 공개 접근 URL 생성 */
    public String buildPublicUrl(String key) {
        String host = URI.create(endpoint).getHost();
        return "https://" + host + "/" + bucketName + "/" + key;
    }

    /** 확장자에 따라 폴더 구분 (로컬 업로드용) */
    private String getFolderByExtension(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".bmp")) return "images";
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".csv")) return "csv";
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "markdown";
        return "etc";
    }
}
