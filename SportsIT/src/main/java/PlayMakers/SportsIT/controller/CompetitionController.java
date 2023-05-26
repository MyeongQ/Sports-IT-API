package PlayMakers.SportsIT.controller;

import PlayMakers.SportsIT.domain.JoinCompetition;
import PlayMakers.SportsIT.domain.JoinCompetitionTemplate;
import PlayMakers.SportsIT.domain.Member;
import PlayMakers.SportsIT.dto.CompetitionDto;
import PlayMakers.SportsIT.dto.JoinCompetitionDto;
import PlayMakers.SportsIT.dto.JoinCountDto;
import PlayMakers.SportsIT.service.CompetitionService;
import PlayMakers.SportsIT.service.JoinCompetitionService;
import PlayMakers.SportsIT.service.JoinCompetitionTemplateService;
import PlayMakers.SportsIT.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import PlayMakers.SportsIT.domain.Competition;

import java.net.URI;
import java.util.*;

import static java.lang.Integer.parseInt;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {
    private final CompetitionService competitionService;
    private final MemberService memberService;
    private final JoinCompetitionService joinCompetitionService;
    private final JoinCompetitionTemplateService joinCompetitionTemplateService;

    @PostMapping
    public ResponseEntity<Competition> createCompetition(@RequestBody CompetitionDto dto,
                                                         @AuthenticationPrincipal User user) throws Exception{
        // 주최자 ID 설정 - 일단 dto에 memberId가 포함된다고 가정
        String hostEmail = user.getUsername(); // 로그인한 회원 ID를 가져옴
        Member host = memberService.findOne(hostEmail);
        dto.setHost(host);

        // 대회 생성
        Competition competition = competitionService.create(dto);

        return ResponseEntity.created(URI.create("/api/competitions/" + competition.getCompetitionId())) // Location Header에 생성된 리소스의 URI를 담아서 보냄
                .body(competition); // 201
    }

    @GetMapping("/all")
    public ResponseEntity<Optional<Competition>> getCompetitions() {
        //List<Competition> competitions = competitionService.getCompetitions();
        Optional<Competition> competitions = null;
        return ResponseEntity.ok(competitions); // 200
    }
    @GetMapping("/slice")
    public ResponseEntity<Slice<Competition>> getCompetitionSlice(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(value = "filterBy", required = false) List<String> filteringConditions,
                                                                  @RequestParam String orderBy,
                                                                  @RequestParam String page,
                                                                  @RequestParam String size) {
        log.info("대회 slice 요청: {} {} {} {} {}", keyword, filteringConditions, orderBy, page, size);

        Slice<Competition> competitions = competitionService.getCompetitionSlice(keyword, filteringConditions, orderBy, parseInt(page), parseInt(size));
        return ResponseEntity.ok(competitions); // 200
    }

    @GetMapping("/{competitionId}")
    public ResponseEntity<Competition> getCompetition(@PathVariable Long competitionId) {
        Competition competition = competitionService.findById(competitionId);
        return ResponseEntity.ok(competition); // 200
    }

    @PutMapping("/{competitionId}")
    public ResponseEntity<Competition> updateCompetition(@PathVariable Long competitionId, @RequestBody CompetitionDto dto) {
        Competition competition = competitionService.update(competitionId, dto);
        return ResponseEntity.ok(competition); // 200
    }

    @DeleteMapping("/{competitionId}")
    public ResponseEntity<Void> deleteCompetition(@PathVariable Long competitionId) {
        competitionService.delete(competitionId);
        return ResponseEntity.noContent().build(); // 204
    }
    @GetMapping("/{competitionId}/join/player/checkJoinable")
    public ResponseEntity<String> isJoinable(@PathVariable Long competitionId,
                                             @AuthenticationPrincipal User user) throws Exception{
        Member member = memberService.findOne(user.getUsername());

        joinCompetitionService.checkAlreadyJoined(member.getUid(), competitionId);
        joinCompetitionService.checkJoinable(competitionId, JoinCompetition.joinType.PLAYER);

        return ResponseEntity.ok("success"); // 200
    }

    @GetMapping("/join/{competitionId}/viewer/checkJoinable")
    public ResponseEntity<String> isJoinableViewer(@PathVariable Long competitionId,
                                             @AuthenticationPrincipal User user) throws Exception{
        Member member = memberService.findOne(user.getUsername());

        joinCompetitionService.checkAlreadyJoined(member.getUid(), competitionId);
        joinCompetitionService.checkJoinable(competitionId, JoinCompetition.joinType.VIEWER);

        return ResponseEntity.ok("success"); // 200
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinCompetition(@RequestBody JoinCompetitionDto joinCompetitionDto,
                                                  @AuthenticationPrincipal User user) throws Exception{

        log.info("대회 참가 요청 Controller: {}", joinCompetitionDto);
        Member member = memberService.findOne(user.getUsername());
        joinCompetitionDto.setUid(member.getUid());
        JoinCompetition joinCompetition = joinCompetitionService.join(joinCompetitionDto);

        // 생성된 리소스의 uri와 함께 201코드, "success" 응답
        return ResponseEntity.created(URI.create("/" + joinCompetition.getId())) // Location Header에 생성된 리소스의 URI를 담아서 보냄
                .body("success"); // 201
    }
    @DeleteMapping("/join")
    public ResponseEntity<String> cancelJoinCompetition(@RequestBody JoinCompetitionDto joincompetitionDto) throws Exception{
        joinCompetitionService.deleteJoinCompetition(joincompetitionDto);

        return ResponseEntity.accepted().body("대회가 취소되었습니다."); // 202
    }

    @GetMapping("/count-join/{competitionId}")
    public ResponseEntity<JoinCountDto> getJoinCompetitionCounts(@PathVariable Long competitionId) throws Exception {
        JoinCountDto result =  joinCompetitionService.countJoinCompetition(competitionId);
        return ResponseEntity.ok(result); // 200
    }

    @PostMapping("/firebase-test")
    public ResponseEntity<String> testFirebase(@RequestBody JoinCompetitionTemplate template) throws Exception {
        log.info("템플릿 생성 요청 Controller: {}", template);
        String docId = joinCompetitionTemplateService.saveTemplate(template);
        return ResponseEntity.ok(docId); // 200
    }

    @PostMapping("/template")
    public ResponseEntity<Object> createTemplate(@RequestBody JoinCompetitionTemplate template) throws Exception {
        log.info("템플릿 생성 요청 Controller: {}", template);
        String docId = joinCompetitionTemplateService.saveTemplate(template);
        Object result = new HashMap<String, Object>() {{
            put("template-id", docId);
        }};
        return ResponseEntity.created(URI.create("/api/competitions/template/" + docId)) // Location Header에 생성된 리소스의 URI를 담아서 보냄
                .body(result); // 201
    }
    @PutMapping("/template/{templateId}")
    public ResponseEntity<Void> updateTemplate(@PathVariable String templateId, @RequestBody JoinCompetitionTemplate template) throws Exception {
        log.info("템플릿 수정 요청 Controller: {}", template);
        joinCompetitionTemplateService.updateTemplate(templateId, template);
        return ResponseEntity.noContent().build(); // 204
    }
//    @GetMapping("/template/{templateId}")
//    public ResponseEntity<JoinCompetitionTemplate> getTemplate(@PathVariable String templateId) throws Exception {
//        log.info("템플릿 조회 요청 Controller: {}", templateId);
//        JoinCompetitionTemplate template = joinCompetitionTemplateService.findTemplate(templateId);
//        return ResponseEntity.ok(template); // 200
//    }
//    @DeleteMapping("/template/{templateId}")
//    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateId) throws Exception {
//        log.info("템플릿 삭제 요청 Controller: {}", templateId);
//        joinCompetitionTemplateService.deleteTemplate(templateId);
//        return ResponseEntity.noContent().build(); // 204
//    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<HashMap<String, Object>> handleNoSuchElementFoundException(EntityNotFoundException exception) {
        // {"code": 1, "message": exception.getMessage()}
        ResponseEntity<HashMap<String, Object>> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new HashMap<String, Object>() {{
                    put("code", 1);
                    put("message", exception.getMessage());
                }});
        return responseEntity;

    }
    @ExceptionHandler
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage()); // 400
    }
}
