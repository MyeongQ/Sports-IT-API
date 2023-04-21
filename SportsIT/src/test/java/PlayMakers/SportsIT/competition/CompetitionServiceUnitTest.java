package PlayMakers.SportsIT.competition;

import PlayMakers.SportsIT.domain.*;
import PlayMakers.SportsIT.dto.CompetitionDto;
import PlayMakers.SportsIT.repository.CompetitionRepository;
import PlayMakers.SportsIT.repository.MemberRepository;
import PlayMakers.SportsIT.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CompetitionServiceUnitTest {
    @Mock
    CompetitionRepository competitionRepository;
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    CompetitionService competitionService;

    MemberType userTypePlayer = MemberType.builder()
            .roleName("ROLE_INSTITUTION")
            .build();
    MemberType userTypeInst = MemberType.builder()
            .roleName("ROLE_INSTITUTION")
            .build();
    MemberType userTypeAdmin = MemberType.builder()
            .roleName("ROLE_INSTITUTION")
            .build();

    @BeforeEach
    public void before() {
        // 호스트 생성
        Member host = Member.builder()
                .pw("1234")
                .name("홍길동")
                .memberType(Collections.singleton(userTypeInst))
                .email("host@gmail.com")
                .phone("010-1234-5678")
                .build();

        System.out.println("host = " + host);
        memberRepository.save(host);
    }
    @Test
    @DisplayName("주최자는 대회를 생성할 수 있다.")
    public void 주최자대회생성() {

        // given 호스트가 대회 생성
        Member host = memberRepository.findByEmail("host@gmail.com");

        // host가 널일 경우
        CompetitionDto dto = CompetitionDto.builder()
                .name("대회이름")
                .host(host)
                .sportCategory(SportCategory.ARM_WRESTLING)
                .totalPrize(10000)
                .content("대회내용")
                .location("대회장소")
                .state(CompetitionState.RECRUITING)
                .build();

        // when 대회 dto 생성
        Competition created = competitionService.create(dto);
        Competition saved = competitionRepository.findById(created.getCompetitionId()).get();

        // then
        assertEquals(created.getCompetitionId(), saved.getCompetitionId());


    }
}