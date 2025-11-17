package dddan.paper_summary.parse.domain;

import dddan.paper_summary.parse.domain.error.DomainException;

/**
 * 추출된 자산(이미지, CSV, 텍스트 등)을 저장하는 포트.
 * 예: S3, 로컬 파일시스템 등
 */
public interface AssetStorage {

    /**
     * 바이트 배열을 저장하고, 접근 가능한 경로나 URL을 반환한다.
     *
     * @param bytes   저장할 데이터
     * @param pathHint 저장 경로/파일명 힌트 (예: papers/{id}/tables/t1.csv)
     * @return 저장된 리소스의 접근 경로/URL
     * @throws DomainException 저장 실패 시
     */
    String store(byte[] bytes, String pathHint) throws DomainException;
}
