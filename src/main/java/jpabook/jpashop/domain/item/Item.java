package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 *  abstract 추상클래스 <br/>
 *  new 연산자를 사용하여 직접 객체를 만들지는 못하고 오직 상속을 통해 자식 클래스만 만드는 용도로만 사용이 가능
 */
@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Book, Album, Movie에 대한 싱글테이블 전략
@DiscriminatorColumn(name = "dtype")
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
}
