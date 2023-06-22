package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리와 아이템과의 다대다 관계 예제샘플
 */
@Entity
@Getter @Setter
public class Category {
    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;
    private String name;
    @ManyToMany
    @JoinTable(name = "category_item", //1:M - M:1로 풀어내는 중간 테이블 매핑
            joinColumns = @JoinColumn(name = "categoriy_id") , //category fk 매핑
            inverseJoinColumns = @JoinColumn(name = "item_id" // item fk 매핑
            )
    ) 
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) // 나의 부모는 ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent") // 바로 위의 부모 필드와 양방향 연관관계 (Self)
    private List<Category> child = new ArrayList<>();

    /**
     * 양방향 연관관계 편의 메소드
     */
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
