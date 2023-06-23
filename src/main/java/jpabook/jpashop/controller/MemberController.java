package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.form.MemberForm;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * [회원 가입] - 회원가입html 출력
     * @param model
     * @return
     */
    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * [회원 가입] - submit
     * @Valid form에 있는 @NotEmpty를 읽어들여 Validation을 처리해준다.
     * @param form
     * @param result : 오류가 result에 담겨서 실행된다. <br/>
     * view단에서 fields.hasErrors('name')를 통해 에러 유무와 validation 내용을 읽어들일 수 있다.
     * @return
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        if (result.hasErrors()) {//에러가 존재하면 화면을 리로딩함과 동시에 result객체를 view단에 넘겨준다.
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        memberService.join(member);
        return "redirect:/";
    }

    /**
     * [회원 목록]
     * @param model : members 키에 members객체를 담아 넘긴다.
     * @return 뷰 페이지
     */
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList.html";
    }
}
