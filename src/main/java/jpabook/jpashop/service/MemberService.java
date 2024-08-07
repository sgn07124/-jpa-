package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)  // 전체에 @Transactional의 읽기 전용을 적용한다.(조회 용도)
@RequiredArgsConstructor  // final이 있는 필드만 가지고 생성자를 만들어줌.
public class MemberService {

    private final MemberRepository memberRepository;  // final을 붙여줘야 함

    /**
     * 회원 가입
     * @param member
     * @return
     */
    @Transactional  // join과 같이 쓰기의 경우에는 @Transactional을 추가하여 readOnly=false를 적용해야 한다.(기본값) -> 이게 우선적으로 적용됨
    public Long join(Member member) {
        validateDuplicateMember(member);  // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {  // 비어있지 않으면? -> 중복되는 이름이 있으면?
            throw new IllegalStateException("이미 존재하는 회원입니다.");  // 예외
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 한 명만 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
