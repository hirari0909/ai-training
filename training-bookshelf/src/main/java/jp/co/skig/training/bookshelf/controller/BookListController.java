package jp.co.skig.training.bookshelf.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.constants.CommonConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.CategoryService;
import jp.co.skig.training.bookshelf.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 書籍一覧コントローラー（BK01）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookListController {

  private final BookService bookService;
  private final CategoryService categoryService;

  /**
   * 書籍一覧を表示する（検索・ソート・ページングに対応）
   */
  @GetMapping("/book/list")
  public String list(
      @RequestParam(required = false) String searchTitle,
      @RequestParam(required = false) String searchAuthor,
      @RequestParam(required = false) Integer searchCategory,
      @RequestParam(required = false) String searchPublisher,
      @RequestParam(required = false) String sortColumn,
      @RequestParam(required = false) String sortOrder,
      @RequestParam(defaultValue = "1") int page,
      HttpSession session,
      Model model) {

    boolean hasSearchParam = StringUtils.hasText(searchTitle)
        || StringUtils.hasText(searchAuthor) || searchCategory != null
        || StringUtils.hasText(searchPublisher);

    String title;
    String author;
    Integer categoryId;
    String publisher;
    if (hasSearchParam) {
      title = StringUtils.hasText(searchTitle) ? searchTitle : null;
      author = StringUtils.hasText(searchAuthor) ? searchAuthor : null;
      categoryId = searchCategory;
      publisher = StringUtils.hasText(searchPublisher) ? searchPublisher : null;
      session.setAttribute(BookConstants.SESSION_SEARCH_TITLE, title);
      session.setAttribute(BookConstants.SESSION_SEARCH_AUTHOR, author);
      session.setAttribute(BookConstants.SESSION_SEARCH_CATEGORY_ID, categoryId);
      session.setAttribute(BookConstants.SESSION_SEARCH_PUBLISHER, publisher);
    } else {
      title = (String) session.getAttribute(BookConstants.SESSION_SEARCH_TITLE);
      author = (String) session.getAttribute(BookConstants.SESSION_SEARCH_AUTHOR);
      categoryId = (Integer) session.getAttribute(BookConstants.SESSION_SEARCH_CATEGORY_ID);
      publisher = (String) session.getAttribute(BookConstants.SESSION_SEARCH_PUBLISHER);
    }

    String col = StringUtils.hasText(sortColumn) ? sortColumn : CommonConstants.DEFAULT_SORT_COLUMN;
    String order = StringUtils.hasText(sortOrder) ? sortOrder : CommonConstants.DEFAULT_SORT_ORDER;

    int pageSize = CommonConstants.PAGE_SIZE;
    int totalCount = bookService.count(title, author, categoryId, publisher);
    int totalPages = Math.max((int) Math.ceil((double) totalCount / pageSize), 1);
    int currentPage = Math.min(Math.max(page, 1), totalPages);

    List<Book> books = bookService.findAll(title, author, categoryId, publisher, col, order,
        currentPage - 1, pageSize);

    model.addAttribute("books", books);
    model.addAttribute("categories", categoryService.findAll());
    model.addAttribute("publishers", bookService.findAllPublishers());
    model.addAttribute("searchTitle", title);
    model.addAttribute("searchAuthor", author);
    model.addAttribute("searchCategory", categoryId);
    model.addAttribute("searchPublisher", publisher);
    model.addAttribute("sortColumn", col);
    model.addAttribute("sortOrder", order);
    model.addAttribute("currentPage", currentPage);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("totalCount", totalCount);

    if (totalCount == 0) {
      model.addAttribute("noDataMessage", MessageUtil.getMessage("bk01.message.nodata"));
    } else if (hasSearchParam) {
      model.addAttribute("searchResultMessage",
          MessageUtil.getMessage("bk01.message.searchresult", totalCount));
    }

    return "book/BK01_BookList";
  }

  /**
   * 検索条件をクリアして一覧にリダイレクトする
   */
  @GetMapping("/book/list/clear")
  public String clearSearch(HttpSession session) {
    session.removeAttribute(BookConstants.SESSION_SEARCH_TITLE);
    session.removeAttribute(BookConstants.SESSION_SEARCH_AUTHOR);
    session.removeAttribute(BookConstants.SESSION_SEARCH_CATEGORY_ID);
    session.removeAttribute(BookConstants.SESSION_SEARCH_PUBLISHER);
    return "redirect:/book/list";
  }
}
