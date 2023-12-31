package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.form.MemberForm;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * 등록 V1 : 요청 값으로 Member 엔티티를 직접 받는다. <br/>
     * 문제점
     * - 엔티티에 프레젠티에션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변하낟.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     * @param member {"name":"hello"} (Body - JSON)
     * @return
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);


    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMember2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 수정 API
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMember(@PathVariable("id") Long id,
                                               @RequestBody
                                               @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    /**
     * 회원 조회 API v1 - 응답값으로 엔터티를 외부에 직접 노출 <br/>
     * 문제점 <br/>
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다. <br/>
     * - 기본적으로 엔티티의 모든 값이 노출된다. <br/>
     * - 응답 스펙을 맞추기 위해 로직이 추가된다.(@JsonIgnore, 별도의 뷰 로직 등등) <br/>
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, <br/>
     *   한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다. <br/>
     * - 엔티티가 변경되면 API 스펙이 변한다. <br/>
     * - 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성으로 해결)
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 회원 조회 API v2 <br/>
     * 엔티티 -> DTO 변환
     * @return Result<T> : {count:__ , data: [{name: ''},{name: ''},,,]}
     */
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(), collect);
    }

    /**
     * 반환할 클래스 <br/>
     * 반환할 값들에 대한 유연성을 보장 (반환할 값이 추가될 수 있음을 대비)
     * @param <T>
     */
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    /**
     * 변환할 클래스
     */
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


    /**
     * 회원 수정 API request 파라미터용 Dto
     */
    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    /**
     * 회원 수정 API update용 응답 Dto
     */
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }


    /**
     * API requset 파라미터를 위한 임시 DTO
     */
    @Data
    static class CreateMemberRequest {
        private String name;
    }

    /**
     * API response 반환을 위한 임시 DTO
     */
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }


}
