package jp.co.skig.training.bookshelf.controller;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Review;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.ReviewService;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 書籍詳細コントローラー（BK02）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookDetailController {

  private final BookService bookService;
  private final ReviewService reviewService;

  /**
   * 書籍詳細画面を表示する
   */
  @GetMapping("/book/detail/{bookId}")
  public String detail(@PathVariable Integer bookId, Model model) {
    Book book = bookService.findById(bookId);
    if (book == null) {
      model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
      model.addAttribute("redirectUrl", "/book/list");
      return "book/error";
    }

    List<Review> reviews = reviewService.findByBookId(bookId);
    model.addAttribute("book", book);
    model.addAttribute("reviews", reviews);
    model.addAttribute("avgRating", reviewService.calculateAverageRating(reviews));
    return "book/BK02_BookDetail";
  }
}
