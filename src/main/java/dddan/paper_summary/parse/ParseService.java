package dddan.paper_summary.parse;

import dddan.paper_summary.ai.client.AiClient;
import dddan.paper_summary.ai.dto.AiFullTextRequest;
import dddan.paper_summary.ai.dto.AiRequestDto;
import dddan.paper_summary.ai.mapper.AiFullTextMapper;
import dddan.paper_summary.ai.mapper.AiRequestMapper;
import dddan.paper_summary.arxiv.dto.ArxivPaperDto;

import dddan.paper_summary.parse.domain.TextExtractor;
import dddan.paper_summary.parse.domain.SectionTextExtractor;
import dddan.paper_summary.parse.domain.TableExtractor;
import dddan.paper_summary.parse.domain.FigureExtractor;

import dddan.paper_summary.parse.domain.model.*;
import dddan.paper_summary.parse.domain.ParseUseCase;
import dddan.paper_summary.parse.domain.error.DomainException;
import dddan.paper_summary.parse.dto.SectionResultDto;
import dddan.paper_summary.parse.dto.TableResultDto;
import dddan.paper_summary.parse.dto.FigureResultDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 논문 PDF 파싱을 담당하는 Application Service
 * - 도메인 레이어의 파서(Text/Section/Table/Figure)를 오케스트레이션
 * - 파싱 결과를 ParseResult(도메인 결과 객체)로 반환
 * - 확장 기능으로 파싱 결과를 AI 서버 요청 DTO로 변환하여 전송 가능
 */
@Service
@RequiredArgsConstructor
public class ParseService implements ParseUseCase {

    private final TextExtractor textExtractor;
    private final TableExtractor tableExtractor;
    private final FigureExtractor figureExtractor;
    private final SectionTextExtractor sectionTextExtractor;

    private final AiFullTextMapper aiFullTextMapper; // 전체 텍스트용 AI 요청 DTO 매퍼
    private final AiClient aiClient;   // AI 서버 통신 클라이언트

    /**
     * 논문 PDF를 파싱하여 도메인 결과(ParseResult)를 생성한다.
     *
     * @param ref 논문 식별자 및 로컬 PDF 경로 정보
     * @return 파싱 성공/실패를 포함한 ParseResult
     */
    @Override
    public ParseResult parse(PaperRef ref) {
        try {
            // 1) 도메인 파서들을 호출하여 각 자산(Asset) 추출
            TextAsset text = textExtractor.extract(ref);
            List<SectionAsset> sections = sectionTextExtractor.extract(ref);
            List<TableAsset> tables = tableExtractor.extract(ref);
            List<FigureAsset> figures = figureExtractor.extract(ref);


            // 2) 파싱 성공 결과를 도메인 결과 객체로 조립
            return ParseResult.success(
                    ref.getPaperId(),
                    text,
                    sections,
                    tables,
                    figures
            );

        } catch (DomainException e) {
            // 3) 도메인 예외 발생 시 실패 결과 반환
            return ParseResult.error(ref.getPaperId(), e.getMessage());
        }
    }

    /**
     * 논문 파싱 + AI 요청 생성 + AI 서버 전송까지 수행하는 확장 메서드
     * - parse() 결과를 재사용
     * - 파싱 결과를 AI 요청용 DTO로 변환
     * - 섹션 기반 요청과 전체 텍스트 기반 요청을 각각 전송
     * @param paper Arxiv API에서 수집한 논문 메타데이터
     * @param formulaPageUrls 수식이 포함된 페이지 이미지(URL) 목록
     * @param ref 논문 PDF 참조 정보
     */
    public void parseAndSendToAI(ArxivPaperDto paper,
                                 List<String> formulaPageUrls,
                                 PaperRef ref) {

        // 1) 기존 파싱 로직 재사용
        ParseResult result = parse(ref);
        if (!result.isSuccess()) {
            return;
        }

        // 2)  AI 요청에 필요한 논문 메타데이터 준비
        String paperId = paper.getArxivId();
        String title = paper.getTitle();

        // 3) 섹션 제목을 이용해 목차(TOC) 문자열 생성
        String toc = result.getSections().stream()
                .map(s -> s.getTitle())
                .collect(Collectors.joining("\n"));

        // 4) 도메인 모델 → AI 요청 DTO 변환
        List<SectionResultDto> sectionDtos = result.getSections().stream()
                .map(s -> new SectionResultDto(/* s 기반으로 채우기 */))
                .toList();

        List<TableResultDto> tableDtos = result.getTables().stream()
                .map(t -> new TableResultDto(/* t 기반으로 */))
                .toList();

        List<FigureResultDto> figureDtos = result.getFigures().stream()
                .map(f -> new FigureResultDto(/* f 기반으로 */))
                .toList();

        // 5) 섹션 단위 AI 요청 DTO 생성
        AiRequestDto sectionRequest =
                AiRequestMapper.toAiRequest(
                        paperId,
                        title,
                        toc,
                        formulaPageUrls,      // 페이지 PNG URL 리스트
                        sectionDtos,
                        tableDtos,
                        figureDtos
                );

        // 6) 전체 텍스트 기반 AI 요청 DTO 생성
        AiFullTextRequest fullTextRequest =
                aiFullTextMapper.toFullText(paper, result);

        // 7) AI 서버로 요청 전송
        aiClient.sendSectionRequest(sectionRequest);
        aiClient.sendFullTextRequest(fullTextRequest);
    }
}
