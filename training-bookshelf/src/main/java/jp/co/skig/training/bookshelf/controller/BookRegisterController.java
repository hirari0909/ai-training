package jp.co.skig.training.bookshelf.controller;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.form.BookRegisterForm;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.CategoryService;
import jp.co.skig.training.bookshelf.util.ExceptionLogger;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 書籍登録コントローラー（BK03-BK05）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookRegisterController {

  private final BookService bookService;
  private final CategoryService categoryService;

  // ========================================
  // BK03: 書籍登録入力画面
  // ========================================

  /**
   * 書籍登録入力画面を表示する
   */
  @GetMapping("/book/create")
  public String newForm(HttpSession session, Model model) {
    BookRegisterForm form = (BookRegisterForm) session.getAttribute(BookConstants.SESSION_REGISTER_FORM);
    model.addAttribute("form", form != null ? form : new BookRegisterForm());
    model.addAttribute("categories", categoryService.findAll());
    return "book/BK03_BookRegisterInput";
  }

  /**
   * 入力内容をバリデーションし、確認画面(BK04)を表示する
   */
  @PostMapping("/book/create/confirm")
  public String confirmRegister(
      @RequestParam String title,
      @RequestParam String author,
      @RequestParam String publisher,
      @RequestParam(required = false) String publishedDate,
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String price,
      @RequestParam(required = false) String description,
      HttpSession session,
      Model model) {

    Map<String, String> errors = BookValidationSupport.validate(
        title, author, publisher, publishedDate, isbn, category, price, description);

    BookRegisterForm form = new BookRegisterForm();
    form.setTitle(title);
    form.setAuthor(author);
    form.setPublisher(publisher);
    form.setIsbn(isbn);
    form.setDescription(description);
    trySetPublishedDate(form, publishedDate);
    trySetCategoryId(form, category);
    trySetPrice(form, price);

    if (!errors.isEmpty()) {
      model.addAttribute("form", form);
      model.addAttribute("errors", errors);
      model.addAttribute("categories", categoryService.findAll());
      return "book/BK03_BookRegisterInput";
    }

    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, form);
    model.addAttribute("form", form);
    model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
    return "book/BK04_BookRegisterConfirm";
  }

  /**
   * 入力画面へキャンセル（一覧へ戻る）
   */
  @GetMapping("/book/create/cancel")
  public String cancelRegister(HttpSession session) {
    session.removeAttribute(BookConstants.SESSION_REGISTER_FORM);
    return "redirect:/book/list";
  }

  // ========================================
  // BK04 → BK05: 書籍登録処理
  // ========================================

  /**
   * 書籍登録処理を実行する
   */
  @PostMapping("/book/create")
  public String register(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
    BookRegisterForm form = (BookRegisterForm) session.getAttribute(BookConstants.SESSION_REGISTER_FORM);
    if (form == null) {
      redirectAttributes.addFlashAttribute("errorMessage", MessageUtil.getMessage("common.session.timeout"));
      return "redirect:/book/create";
    }

    try {
      if (bookService.isDuplicateIsbn(form.getIsbn())) {
        model.addAttribute("form", form);
        model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
        model.addAttribute("errorMessage", MessageUtil.getMessage("validation.duplicate.isbn"));
        return "book/BK04_BookRegisterConfirm";
      }

      Book book = new Book();
      book.setTitle(form.getTitle());
      book.setAuthor(form.getAuthor());
      book.setPublisher(form.getPublisher());
      book.setPublishedDate(form.getPublishedDate());
      book.setIsbn(form.getIsbn());
      book.setCategoryId(form.getCategoryId());
      book.setPrice(form.getPrice());
      book.setDescription(form.getDescription());
      bookService.register(book);

      session.removeAttribute(BookConstants.SESSION_REGISTER_FORM);
      return "redirect:/book/create/complete?bookId=" + book.getBookId();
    } catch (Exception e) {
      ExceptionLogger.log(e);
      model.addAttribute("form", form);
      model.addAttribute("categoryName", resolveCategoryName(form.getCategoryId()));
      model.addAttribute("errorMessage", MessageUtil.getMessage("db.error.insert"));
      return "book/BK04_BookRegisterConfirm";
    }
  }

  /**
   * 書籍登録完了画面を表示する
   */
  @GetMapping("/book/create/complete")
  public String registerComplete(@RequestParam Integer bookId, Model model) {
    model.addAttribute("bookId", bookId);
    return "book/BK05_BookRegisterComplete";
  }

  private void trySetPublishedDate(BookRegisterForm form, String publishedDate) {
    try {
      form.setPublishedDate(LocalDate.parse(publishedDate));
    } catch (Exception e) {
      form.setPublishedDate(null);
    }
  }

  private void trySetCategoryId(BookRegisterForm form, String category) {
    try {
      form.setCategoryId(Integer.valueOf(category));
    } catch (Exception e) {
      form.setCategoryId(null);
    }
  }

  private void trySetPrice(BookRegisterForm form, String price) {
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
