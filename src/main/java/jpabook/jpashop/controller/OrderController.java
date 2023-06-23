package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    /**
     * [상품 주문] 상품주문html
     * @param model
     * @return 상품주문html
     */
    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        model.addAttribute("members", members);
        model.addAttribute("items", items);
        return "order/orderForm";
    }

    /**
     * [상품 주문] submit 상품 저장
     * @param model
     * @return
     */
    @PostMapping("/order")
    public String order(Long memberId, Long itemId, int count) {
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }

    /**
     * [주문 내역] 주문내역html
     * @param model
     * @param orderSearch : @ModelAttribute에 의해 model에 담겨서 반환된다.
     * @return 주문내역html
     */
    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";
    }

    /**
     * [주문 취소] 주문 취소 <br/>
     * orderList.html 최 하단 자바스크립트 코드를 통한 매핑
     * @param model
     * @return 주문내역html
     */
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
