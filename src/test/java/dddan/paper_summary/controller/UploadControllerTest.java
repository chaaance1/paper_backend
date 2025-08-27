package dddan.paper_summary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUploadTempPath() throws Exception {
        // 테스트할 PDF 파일 경로 (실제 존재해야 함)
        Path fixedPath = Path.of("pdfs/2103.00112v3.pdf");

        if (!Files.exists(fixedPath)) {
            throw new IllegalStateException("테스트 파일이 존재하지 않음: " + fixedPath);
        }

        // POST 요청 실행
        MvcResult result = mockMvc.perform(post("/api/upload/temp-path")
                        .param("path", fixedPath.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andReturn();

        // 응답 본문을 JSON으로 파싱하여 'url' 추출
        String response = result.getResponse().getContentAsString();
        String extractedUrl = objectMapper.readTree(response).get("url").asText();

        System.out.println("업로드된 파일의 URL: " + extractedUrl);

        // 링크 형식 검증
        assertThat(extractedUrl)
                .as("오브젝트 스토리지 URL이어야 합니다")
                .startsWith("https://kr.object.ncloudstorage.com/");
    }
}
