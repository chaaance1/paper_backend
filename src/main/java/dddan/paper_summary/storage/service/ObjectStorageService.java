package dddan.paper_summary.storage.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final Storage storage;
    private final String paperbiteBucketName;

    /**
     * 논문 페이지 이미지(PNG) Google Cloud Storage에 업로드 → 객체 경로 문자열 반환
     */
    public String uploadPageImage(byte[] bytes, String paperId, int pageNumber) {
        // 객체 경로 및 이름 지정 (ex.papers/2412.03801v1/pages/page-001.png)
        String objectName = String.format(
                "papers/%s/pages/page-%03d.png", paperId, pageNumber
        );

        // GCS에 업로드할 메타데이터 정보 생성
        BlobInfo blobInfo = BlobInfo.newBuilder(paperbiteBucketName, objectName)  // 버킷 이름 및 버킷의 경로 지정
                .setContentType("image/png")  // Content-Type 지정
                .build();

        storage.create(blobInfo, bytes);  // 업로드
        return objectName;  // 업로드된 파일의 경로 리턴
    }

    /**
     * 논문 내부 파싱 이미지(JPG) Google Cloud Storage에 업로드 → 객체 경로 문자열 반환
     */
    public String uploadGeneralImage(byte[] bytes, String paperId, int index) {
        // 객체 경로 및 이름 지정 (ex.papers/2412.03801v1/figures/figure-001.jpg)
        String objectName = String.format(
                "papers/%s/figures/figure-%03d.jpg", paperId, index
        );

        // GCS에 업로드할 메타데이터 정보 생성
        BlobInfo blobInfo = BlobInfo.newBuilder(paperbiteBucketName, objectName)  // 버킷 이름 및 버킷의 경로 지정
                .setContentType("image/jpeg")  // Content-Type 지정
                .build();

        storage.create(blobInfo, bytes);  // 업로드
        return objectName;   // 업로드된 파일의 경로 리턴
    }

    /**
     * 논문 내부 파싱 표(CSV) Google Cloud Storage에 업로드 → 객체 경로 문자열 반환
     */
    public String uploadTableCsv(String csvContent, String paperId, int index) {
        // 객체 경로 및 이름 지정 (ex.papers/2412.03801v1/tables/table-001.csv)
        String objectName = String.format(
                "papers/%s/tables/table-%03d.csv", paperId, index
        );

        BlobInfo blobInfo = BlobInfo.newBuilder(paperbiteBucketName, objectName)  // 버킷 이름 및 버킷의 경로 지정
                .setContentType("text/csv")  // Content-Type 지정
                .build();

        // 표를 byte 배열로 인코딩
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        storage.create(blobInfo, bytes);  // 업로드
        return objectName;   // 업로드된 파일의 경로 리턴
    }
}
