package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    @NotEmpty
    private String name;
    @Embedded
    private Address address;

    @JsonIgnore // Member를 조회할때 Member에 대한 엔터티만 조회할 수 있도록 무시한다.
    @OneToMany(mappedBy = "member")// 연관관계 거울:읽기전용 (Order에 있는 member필드와 매핑) _ 양방향 연관관계 - 여러개의 주문은 각각 하나의 member를 갖는다.
    private List<Order> orders = new ArrayList<>(); //값을 넣는다고 해서 fk값이 변경되지 않는다.

}
