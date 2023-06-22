package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter @Setter
@DiscriminatorValue("B") // Item테이블의 dtype컬럼에 추가되는 구분 값
public class Book extends Item{
    private String author;
    private String isbn;
}
