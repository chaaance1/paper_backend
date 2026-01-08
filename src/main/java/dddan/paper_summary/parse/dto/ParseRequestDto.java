package dddan.paper_summary.parse.dto;

import lombok.*;

/**
 * PDF 파싱 요청 시 사용되는 요청 DTO
 * - 클라이언트(API 호출자)가 서버에 PDF 파싱을 요청할 때 전달하는 메타데이터
 * - 실제 PDF 파일(MultipartFile)과 함께 사용되며,
 *   이 DTO는 파일 자체가 아닌 "식별 및 관리용 정보"만을 담는다.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParseRequestDto {
    private Long paperId;     // 선택: DB 연동용
    private String fileName;    // 업로드 파일명
}
