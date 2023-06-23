package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.form.BookForm;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * [상품 등록] html <br/>
     * @param model
     * @return 상품 등록 View html
     */
    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    /**
     * [상품 등록] submit 저장 <br/>
     * @param form
     * @return : 상품 목록 페이지로 redirect 전환
     */
    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);
        return "redirect:/items";
    }

    /**
     * [상품 목록] html
     * @param model
     * @return
     */
    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * [상품 수정] html
     * @param itemId : 상품번호
     * @param model
     * @return : 상품 수정 View html
     */
    @GetMapping("/items/{itemId}/edit") //{itemId} : PathVariable
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book findItem = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();
        form.setId(findItem.getId());
        form.setName(findItem.getName());
        form.setPrice(findItem.getPrice());
        form.setStockQuantity(findItem.getStockQuantity());
        form.setAuthor(findItem.getAuthor());
        form.setIsbn(findItem.getIsbn());
        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /**
     * [상품 수정] submit 저장 <br/>
     * 매핑주소 : action을 생략하면 현재 URL을 그대로 사용 <br/>
     * updateItemForm의 submit action url이 지정되어있지 않으므로 <br/>
     * 상품수정 html을 띄워주는 매핑주소와 일치해야 한다
     * @param form
     * @return 상품 목록 redirect
     */
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable("itemId") Long itemId, BookForm form) {
//        Book book = new Book(); //준영속 엔티티 - merger를 통해 수정해야함.
//        book.setId(itemId);
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);
        itemService.updateItem(itemId, form);
        return "redirect:/items";
    }
}
