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

        // Controller는 파일을 한 번만 열고, UseCase에 "레퍼런스"를 넘깁니다.
        // UseCase 내부에서 스트리밍/임시파일/페이지 병렬화 등 처리.
        PaperRef ref = PaperRef.builder()
                .paperId(paperId)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .inputStreamSupplier(file::getInputStream) // 필요할 때만 읽음 (지연 로딩)
                .build();

        var result = parseUseCase.parse(ref);          // 전체 파싱은 app 레이어에서
        var resp   = mapper.toResponseDto(result);     // DTO 변환만 api에서

        return ResponseEntity.ok(resp);
    }
}
