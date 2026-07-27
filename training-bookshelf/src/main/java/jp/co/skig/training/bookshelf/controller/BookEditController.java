package jp.co.skig.training.bookshelf.controller;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.form.BookEditForm;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.CategoryService;
import jp.co.skig.training.bookshelf.util.ExceptionLogger;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 書籍編集コントローラー（BK06-BK08）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookEditController {

  private final BookService bookService;
  private final CategoryService categoryService;

  // ========================================
  // BK06: 書籍編集入力画面
  // ========================================

  /**
   * 書籍編集入力画面を表示する
   */
  @GetMapping("/book/edit/{bookId}")
  public String editForm(@PathVariable Integer bookId, HttpSession session, Model model) {
    BookEditForm sessionForm = (BookEditForm) session.getAttribute(BookConstants.SESSION_EDIT_FORM);
    BookEditForm form;
    if (sessionForm != null && bookId.equals(sessionForm.getBookId())) {
      form = sessionForm;
    } else {
      Book book = bookService.findById(bookId);
      if (book == null) {
        model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
        model.addAttribute("redirectUrl", "/book/list");
        return "book/error";
      }
      form = toEditForm(book);
    }
    model.addAttribute("form", form);
    model.addAttribute("categories", categoryService.findAll());
    return "book/BK06_BookEditInput";
  }

  /**
   * 入力内容をバリデーションし、編集確認画面(BK07)を表示する
   */
  @PostMapping("/book/edit/{bookId}/confirm")
  public String confirmEdit(
      @PathVariable Integer bookId,
      @RequestParam String title,
      @RequestParam String author,
      @RequestParam String publisher,
      @RequestParam(required = false) String publishedDate,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String price,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String recommended,
      @RequestParam String updatedAt,
      HttpSession session,
      Model model) {

    Map<String, String> errors = BookValidationSupport.validate(
        title, author, publisher, publishedDate, isbn, category, price, description);

    BookEditForm form = new BookEditForm();
    form.setBookId(bookId);
    form.setTitle(title);
    form.setAuthor(author);
    form.setPublisher(publisher);
    form.setIsbn(isbn);
    form.setDescription(description);
    form.setRecommended(recommended != null);
    form.setUpdatedAt(parseUpdatedAt(updatedAt));
    trySetPublishedDate(form, publishedDate);
    trySetCategoryId(form, category);
    trySetPrice(form, price);

    if (!errors.isEmpty()) {
      model.addAttribute("form", form);
      model.addAttribute("errors", errors);
      model.addAttribute("categories", categoryService.findAll());
      return "book/BK06_BookEditInput";
    }

    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);
    model.addAttribute("form", form);
    model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
    return "book/BK07_BookEditConfirm";
  }

  /**
   * 編集をキャンセルし、詳細画面へ戻る
   */
  @GetMapping("/book/edit/{bookId}/cancel")
  public String cancelEdit(@PathVariable Integer bookId, HttpSession session) {
    session.removeAttribute(BookConstants.SESSION_EDIT_FORM);
    return "redirect:/book/detail/" + bookId;
  }

  // ========================================
  // BK07 → BK08: 書籍更新処理
  // ========================================

  /**
   * 書籍更新処理を実行する
   */
  @PostMapping("/book/edit/{bookId}")
  public String update(@PathVariable Integer bookId, HttpSession session,
      RedirectAttributes redirectAttributes, Model model) {
    BookEditForm form = (BookEditForm) session.getAttribute(BookConstants.SESSION_EDIT_FORM);
    if (form == null || !bookId.equals(form.getBookId())) {
      redirectAttributes.addFlashAttribute("errorMessage", MessageUtil.getMessage("common.session.timeout"));
      return "redirect:/book/edit/" + bookId;
    }

    try {
      if (bookService.isDuplicateIsbn(form.getIsbn(), bookId)) {
        model.addAttribute("form", form);
        model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
        model.addAttribute("errorMessage", MessageUtil.getMessage("validation.duplicate.isbn"));
        return "book/BK07_BookEditConfirm";
      }

      Book book = new Book();
      book.setBookId(bookId);
      book.setTitle(form.getTitle());
      book.setAuthor(form.getAuthor());
      book.setPublisher(form.getPublisher());
      book.setPublishedDate(form.getPublishedDate());
      book.setIsbn(form.getIsbn());
      book.setCategoryId(form.getCategoryId());
      book.setPrice(form.getPrice());
      book.setDescription(form.getDescription());
      book.setRecommended(form.getRecommended());
      book.setUpdatedAt(form.getUpdatedAt());

      int updateCount = bookService.update(book);
      if (updateCount == 0) {
        session.removeAttribute(BookConstants.SESSION_EDIT_FORM);
        redirectAttributes.addFlashAttribute("errorMessage", MessageUtil.getMessage("error.concurrent.update"));
        return "redirect:/book/detail/" + bookId;
      }

      session.removeAttribute(BookConstants.SESSION_EDIT_FORM);
      return "redirect:/book/edit/complete?bookId=" + bookId;
    } catch (Exception e) {
      ExceptionLogger.log(e);
      model.addAttribute("form", form);
      model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
      model.addAttribute("errorMessage", MessageUtil.getMessage("db.error.update"));
      return "book/BK07_BookEditConfirm";
    }
  }

  /**
   * 書籍編集完了画面を表示する
   */
  @GetMapping("/book/edit/complete")
  public String editComplete(@RequestParam Integer bookId, Model model) {
    model.addAttribute("bookId", bookId);
    return "book/BK08_BookEditComplete";
  }

  private BookEditForm toEditForm(Book book) {
    BookEditForm form = new BookEditForm();
    form.setBookId(book.getBookId());
    form.setTitle(book.getTitle());
    form.setAuthor(book.getAuthor());
    form.setPublisher(book.getPublisher());
    form.setPublishedDate(book.getPublishedDate());
    form.setIsbn(book.getIsbn());
    form.setCategoryId(book.getCategoryId());
    form.setPrice(book.getPrice());
    form.setDescription(book.getDescription());
    form.setRecommended(book.getRecommended());
    form.setUpdatedAt(book.getUpdatedAt());
    return form;
  }

  private LocalDateTime parseUpdatedAt(String updatedAt) {
    try {
      return LocalDateTime.parse(updatedAt);
    } catch (Exception e) {
      return null;
    }
  }

  private void trySetPublishedDate(BookEditForm form, String publishedDate) {
    try {
      form.setPublishedDate(LocalDate.parse(publishedDate));
    } catch (Exception e) {
      form.setPublishedDate(null);
    }
  }

  private void trySetCategoryId(BookEditForm form, String category) {
    try {
      form.setCategoryId(Integer.valueOf(category));
    } catch (Exception e) {
      form.setCategoryId(null);
    }
  }

  private void trySetPrice(BookEditForm form, String price) {
    try {
      form.setPrice(Integer.valueOf(price));
    } catch (Exception e) {
      form.setPrice(null);
    }
  }

  private String resolveCategoryName(Integer categoryId) {
    List<Category> categories = categoryService.findAll();
    return categories.stream()
        .filter(c -> c.getCategoryId().equals(categoryId))
        .map(Category::getCategoryName)
        .findFirst()
        .orElse("");
  }
}
