package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ApiMemberController {

    private final MemberService memberService;

    /**
     * 조회 v1 : 응답 값으로 엔티티를 직접 외부에 노출함 -> List<Member>
     * 문제점 :
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다. -> Member 내에 Order 등 까지 모두 노출됨
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. -> @JsonIgnore 을 엔티티에 추가함(하나의 용도로 쓰기에는 부적절함)
     *       -> 실무에서는 같은 엔티티에 대해 API 용도가 다양한데 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어려움
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다. -> 별도의 Result 클래스 생성으로 해결 가능
     * 결론 :
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     * 조회 결과 : -> 모든 데이터가 노출됨
     * [
     *     {
     *         "id": 52,
     *         "name": "member1",
     *         "address": {
     *             "city": "서울",
     *             "street": "test1",
     *             "zipcode": "11111"
     *         },
     *         "orders": []
     *     },
     *     {
     *         "id": 53,
     *         "name": "member2",
     *         "address": {
     *             "city": "부산",
     *             "street": "222",
     *             "zipcode": "22222"
     *         },
     *         "orders": []
     *     }
     * ]
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 조회 v2 : 응답 값으로 엔티티가 아닌 별도의 DTO 사용
     * 특징 :
     * - 엔티티를 DTO로 변환해서 반환
     * - 엔티티가 변해도 API 스펙이 변경되지 않는다.
     * - Result 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있다.
     * 결론 :
     * - API를 만들 때는 파라미터를 받는 내보내든 절대로 엔티티를 노출하지 말 것!!!
     * - 무조건 중간에 API 스펙에 맞는 DTO를 만들고 이것을 활용해라!!
     * 조회 결과 :
     * {
     *     "data": [
     *         {
     *             "name": "one"
     *         },
     *         {
     *             "name": "three"
     *         },
     *         {
     *             "name": "member1"
     *         },
     *         {
     *             "name": "member2"
     *         }
     *     ]
     * }
     */
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    /**
     * 등록 v1 : 요청 값으로 Member 엔티티를 직접 만든다.
     * 문제점 :
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론 :
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {  // 이렇게 Member를 외부에 노출시키면 안되며 DTO를 파라미터로 받아야 함
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 등록 v2 : 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     * 특징 :
     * - CreateMemberRequest를 Member 엔티티 대신에 RequestBody와 매핑한다.
     * - 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다.
     * - 엔티티와 API 스펙을 명확하게 분리할 수 있다.
     * - 엔티티가 변해도 API 스펙이 변하지 않는다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {  // @RequestBody에 dto를 매핑
        Member member = new Member();
        member.setName(request.name);

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 정보 수정 v2 : 회원 정보를 부분 수정한다.
     * 특징 :
     * - PUT 방식 : 전체 업데이트를 할 때 사용
     * - PATCH(or POST) 방식 : 부분 업데이트를 할 때 사용
     */
    @PatchMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {  // DTO를 파라미터에 매핑
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data  // DTO는 데이터가 왔다갔다 하기 때문에 @Data를 써도 되지만 엔티티는 생각하고 써야된다.
    @AllArgsConstructor
    static class UpdateMemberRequest {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {  // dto
        @NotEmpty  // 검증 로직을 dto에 추가하여 엔티티와 API 스펙을 분리한다.
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
