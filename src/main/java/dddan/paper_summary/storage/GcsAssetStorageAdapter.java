package dddan.paper_summary.storage;

import dddan.paper_summary.parse.domain.AssetStorage;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.storage.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse 도메인의 AssetStorage 포트를 GCS(ObjectStorageService)로 연결하는 어댑터
 * store()는 "외부에서 바로 접근 가능한 링크(URL)"를 반환한다
 * - 내부적으로는 ObjectStorageService가 objectName(키)을 만들고 업로드한다
 * - 어댑터가 objectName -> public URL로 조합해 반환한다.
 * ⚠ 전제:
 * - 버킷(또는 papers/** prefix)이 public read 가능해야 해당 URL로 접근 가능
 */
@Component
@RequiredArgsConstructor
public class GcsAssetStorageAdapter implements AssetStorage {

    private final ObjectStorageService objectStorageService;

    @Value("${paperbite.bucket-name}")
    private String bucketName;

    // public URL base
    // (필요하면 CDN 도메인으로 바꿔도 됨)
    private static final String GCS_PUBLIC_BASE = "https://storage.googleapis.com/";

    // papers/{paperId}/tables/table-003.csv 같은 힌트를 분석하기 위한 패턴
    private static final Pattern PAPER_ID_PATTERN = Pattern.compile("^papers/([^/]+)/");
    private static final Pattern INDEX_PATTERN = Pattern.compile("-(\\d{1,})\\.(?:png|jpg|jpeg|csv)$");

    @Override
    public String store(byte[] bytes, String pathHint) throws DomainException {
        if (bytes == null || bytes.length == 0) {
            throw new DomainException("AssetStorage.store(): bytes is empty");
        }
        if (pathHint == null || pathHint.isBlank()) {
            throw new DomainException("AssetStorage.store(): pathHint is blank");
        }

        try {
            String paperId = extractPaperId(pathHint);
            int index = extractIndex(pathHint);

            // 어떤 타입인지 판별
            String objectName;
            String lower = pathHint.toLowerCase();

            if (lower.contains("/tables/")) {
                // table은 CSV 문자열 업로드 메서드만 있으므로 bytes -> String 변환
                String csv = new String(bytes, StandardCharsets.UTF_8);
                objectName = objectStorageService.uploadTableCsv(csv, paperId, index);

            } else if (lower.contains("/pages/")) {
                // 페이지 PNG 업로드
                // 여기서 index는 pageNumber로 사용한다고 가정
                objectName = objectStorageService.uploadPageImage(bytes, paperId, index);

            } else if (lower.contains("/figures/")) {
                // figure 업로드 (현재 서비스는 jpg로 저장함)
                objectName = objectStorageService.uploadGeneralImage(bytes, paperId, index);

            } else {
                // 힌트가 애매하면 figure로 처리(프로젝트 정책에 맞게 수정 가능)
                objectName = objectStorageService.uploadGeneralImage(bytes, paperId, index);
            }

            // objectName(키) -> public URL 조합 후 반환
            return toPublicUrl(objectName);

        } catch (Exception e) {
            // DomainException으로 감싸서 도메인 계층으로 전달
            throw new DomainException("GCS upload failed: " + e.getMessage(), e);
        }
    }

    private String toPublicUrl(String objectName) {
        // objectName: papers/xxx/figures/figure-001.jpg
        return GCS_PUBLIC_BASE + bucketName + "/" + objectName;
    }

    /**
     * pathHint 예: papers/{paperId}/figures/figure-003.jpg
     */
    private String extractPaperId(String pathHint) {
        Matcher m = PAPER_ID_PATTERN.matcher(pathHint);
        if (!m.find()) {
            // papers/가 없거나 형식이 다르면 tmp로 폴백
            return "tmp";
        }
        return m.group(1);
    }

    /**
     * pathHint 예: .../figure-003.jpg, .../table-12.csv, .../page-001.png
     *  - 마지막 "-숫자.확장자" 형태에서 숫자 추출
     * 형식이 다르면 1로 폴백
     */
    private int extractIndex(String pathHint) {
        Matcher m = INDEX_PATTERN.matcher(pathHint);
        if (!m.find()) return 1;
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
