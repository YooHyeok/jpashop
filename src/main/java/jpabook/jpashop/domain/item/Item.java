package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();
    
    //===================비즈니스 로직===================/
    
    /**
     * Stock 재고수량 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * Stock 재고수량 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity; //남은 수량 (전체 수량 - 주문 수량)
        if (restStock < 0) { // 남은 수량이 0보다 작으면 Exception
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
