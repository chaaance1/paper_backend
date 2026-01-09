package dddan.paper_summary.parse.controller;

import dddan.paper_summary.parse.dto.ParseRequestDto;
import dddan.paper_summary.parse.dto.ParseResponseDto;
import dddan.paper_summary.parse.domain.model.PaperRef;
import dddan.paper_summary.parse.domain.ParseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF 파일 업로드 요청을 받아 ParseUseCase에 위임하고 결과를 JSON으로 반환하는 API Controller
 * - Controller는 파싱 로직을 직접 수행하지 않는다.
 * - 파일 처리, PDF 해석, 병렬 처리 등은 모두 app 레이어(ParseUseCase)에 위임한다.
 * - 이 계층은 HTTP ↔ 도메인 경계 역할만 담당한다.
 */

@RestController
@RequestMapping("/api/parse")
@RequiredArgsConstructor
@Validated
public class ParseController {

    private final ParseUseCase parseUseCase;      // app 레이어 유스케이스(오케스트레이터)
    private final ParseDtoMapper mapper;          // DTO ↔ 도메인 매핑 전담

    @PostMapping(
            value = "/pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ParseResponseDto> parsePdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "meta", required = false) ParseRequestDto meta
    ) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // meta는 선택값
        Long paperId = (meta != null) ? meta.getPaperId() : null;

        // Controller는 PDF를 직접 열거나 읽지 않는다
        // UseCase 내부에서 스트리밍/임시파일/페이지 병렬화 등 처리
        PaperRef ref = PaperRef.builder()
                .paperId(paperId)
                .filename(file.getOriginalFilename())
                .inputStreamSupplier(file::getInputStream) // 필요할 때만 읽음 (지연 로딩)
                .build();

        var result = parseUseCase.parse(ref);          // 전체 파싱은 app 레이어에서
        var resp   = mapper.toResponseDto(result);     // DTO 변환만 api에서

        return ResponseEntity.ok(resp);
    }
}
