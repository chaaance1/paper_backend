package dddan.paper_summary.storage.controller;

import dddan.paper_summary.storage.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")

public class UploadController {

    private final ObjectStorageService objectStorageService;

    /** 서버에 있는 임시파일 경로를 받아 오브젝트 스토리지에 업로드하고 URL 반환 */
    @PostMapping("/temp-path")
    public ResponseEntity<?> uploadTempPath(@RequestParam("path") String tempPath) {
        try {
            Path path = Path.of(tempPath);
            String url = objectStorageService.uploadLocalFile(path, true); // 자동 폴더 분류됨
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("업로드 실패: " + e.getMessage());
        }
    }

}
