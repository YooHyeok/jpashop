package jpabook.jpashop.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수입니다.") //javax.validation 값이 비어있으면 오류 메시지 출력
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
