package dddan.paper_summary.storage.controller;

import dddan.paper_summary.storage.service.ObjectStorageService;
import dddan.paper_summary.storage.dto.StorageObjectResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")   // 굳이 /test 안 붙여도 됨
public class ObjectStorageController {

    private final ObjectStorageService objectStorageService;

    /**
     * PNG 페이지 이미지 업로드 테스트
     */
    @PostMapping("/page")
    public ResponseEntity<StorageObjectResponseDto> uploadPage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "test-paper") String paperId,
            @RequestParam(defaultValue = "1") int pageNumber
    ) throws IOException {
        byte[] bytes = file.getBytes();
        String objectName = objectStorageService.uploadPageImage(bytes, paperId, pageNumber);
        return ResponseEntity.ok(
                new StorageObjectResponseDto("paperbite", objectName)
        );
    }

    /**
     * JPG 내부 이미지 업로드 테스트
     */
    @PostMapping("/figure")
    public ResponseEntity<StorageObjectResponseDto> uploadFigure(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "test-paper") String paperId,
            @RequestParam(defaultValue = "1") int index
    ) throws IOException {
        byte[] bytes = file.getBytes();
        String objectName = objectStorageService.uploadGeneralImage(bytes, paperId, index);
        return ResponseEntity.ok(
                new StorageObjectResponseDto("paperbite", objectName)
        );
    }

    /**
     * CSV 표 업로드 테스트
     */
    @PostMapping("/table")
    public ResponseEntity<StorageObjectResponseDto> uploadTable(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "test-paper") String paperId,
            @RequestParam(defaultValue = "1") int index
    ) throws IOException {
        String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        String objectName = objectStorageService.uploadTableCsv(csvContent, paperId, index);
        return ResponseEntity.ok(
                new StorageObjectResponseDto("paperbite", objectName)
        );
    }
}
