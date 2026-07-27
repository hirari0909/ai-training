package jp.co.skig.training.bookshelf.controller;

import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.util.ExceptionLogger;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 書籍削除コントローラー（BK09-BK10）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookDeleteController {

  private final BookService bookService;

  // ========================================
  // BK09: 書籍削除確認画面
  // ========================================

  /**
   * 書籍削除確認画面を表示
   */
  @GetMapping("/book/delete/confirm/{bookId}")
  public String confirmDelete(@PathVariable Integer bookId, Model model) {
    Book book = bookService.findById(bookId);
    if (book == null) {
      model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
      model.addAttribute("redirectUrl", "/book/list");
      return "book/error";
    }

    model.addAttribute("book", book);
    return "book/BK09_BookDeleteConfirm";
  }

  /**
   * 書籍削除処理
   */
  @PostMapping("/book/delete/{bookId}")
  public String delete(@PathVariable Integer bookId, Model model) {
    try {
      int deleteCount = bookService.delete(bookId);
      if (deleteCount == 0) {
        model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
        model.addAttribute("redirectUrl", "/book/list");
        return "book/error";
      }
      return "redirect:/book/delete/complete";
    } catch (Exception e) {
      ExceptionLogger.log(e);
      Book book = bookService.findById(bookId);
      model.addAttribute("book", book);
      model.addAttribute("errorMessage", MessageUtil.getMessage("db.error.delete"));
      return "book/BK09_BookDeleteConfirm";
    }
  }

  // ========================================
  // BK10: 書籍削除完了画面
  // ========================================

  /**
   * 書籍削除完了画面を表示
   */
  @GetMapping("/book/delete/complete")
  public String deleteComplete() {
    return "book/BK10_BookDeleteComplete";
  }
}
