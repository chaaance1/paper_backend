package dddan.paper_summary.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorageObjectResponseDto {

    // GCS 버킷 이름 (ex. "paperbite")
    private String bucket;

    // 버킷 안 객체 경로 (ex. "papers/2412.03801v1/pages/page-001.png")
    private String objectName;
}
