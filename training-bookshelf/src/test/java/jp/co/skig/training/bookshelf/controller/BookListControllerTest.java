package jp.co.skig.training.bookshelf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.CategoryService;
import jp.co.skig.training.bookshelf.util.MessageUtilTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookListController.class)
class BookListControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @MockitoBean
  private CategoryService categoryService;

  @BeforeAll
  static void setUpMessageSource() {
    MessageUtilTestSupport.init();
  }

  private List<Book> createBooks(int n) {
    List<Book> books = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      Book book = new Book();
      book.setBookId(i);
      book.setTitle("タイトル" + i);
      books.add(book);
    }
    return books;
  }

  @Test
  void BK01_001_初期表示成功_検索条件なし() throws Exception {
    // Given: 書籍3件、カテゴリなし、セッション未設定
    when(bookService.count(isNull(), isNull(), isNull(), isNull())).thenReturn(3);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(3));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK01_BookList"))
        .andExpect(model().attribute("totalCount", 3))
        .andExpect(model().attribute("currentPage", 1));
  }

  @Test
  void BK01_002_検索条件指定で検索しセッション保存() throws Exception {
    // Given: タイトル検索でヒット1件
    when(bookService.count(eq("Java"), isNull(), isNull(), isNull())).thenReturn(1);
    when(bookService.findAll(eq("Java"), isNull(), isNull(), isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(1));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When
    var result = mockMvc.perform(get("/book/list").param("searchTitle", "Java"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("searchTitle", "Java"))
        .andReturn();

    // Then: セッションに検索条件が保存されている
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    org.assertj.core.api.Assertions.assertThat(session.getAttribute(BookConstants.SESSION_SEARCH_TITLE))
        .isEqualTo("Java");
  }

  @Test
  void BK01_003_セッションから検索条件復元() throws Exception {
    // Given: セッションに検索条件あり、パラメータなし
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_SEARCH_TITLE, "夏目漱石");
    when(bookService.count(eq("夏目漱石"), isNull(), isNull(), isNull())).thenReturn(1);
    when(bookService.findAll(eq("夏目漱石"), isNull(), isNull(), isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(1));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("searchTitle", "夏目漱石"));
  }

  @Test
  void BK01_012_カテゴリのみ指定で検索条件ありと判定() throws Exception {
    // Given: タイトル・著者は未指定でカテゴリのみ指定
    when(bookService.count(isNull(), isNull(), eq(1), isNull())).thenReturn(2);
    when(bookService.findAll(isNull(), isNull(), eq(1), isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(2));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then: 検索条件ありとして扱われ、検索結果メッセージが表示される
    mockMvc.perform(get("/book/list").param("searchCategory", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("searchCategory", 1))
        .andExpect(model().attribute("searchResultMessage", "2件の書籍が見つかりました"));
  }

  @Test
  void BK01_013_出版社のみ指定で検索条件ありと判定() throws Exception {
    // Given: タイトル・著者・カテゴリは未指定で出版社のみ指定
    when(bookService.count(isNull(), isNull(), isNull(), eq("オーム社"))).thenReturn(4);
    when(bookService.findAll(isNull(), isNull(), isNull(), eq("オーム社"), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(4));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then: 検索条件ありとして扱われ、検索結果メッセージが表示される
    mockMvc.perform(get("/book/list").param("searchPublisher", "オーム社"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("searchPublisher", "オーム社"))
        .andExpect(model().attribute("searchResultMessage", "4件の書籍が見つかりました"));
  }

  @Test
  void BK01_004_ソート指定あり() throws Exception {
    // Given
    when(bookService.count(any(), any(), any(), any())).thenReturn(0);
    when(bookService.findAll(any(), any(), any(), any(), eq("title"), eq("ASC"), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list").param("sortColumn", "title").param("sortOrder", "ASC"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("sortColumn", "title"))
        .andExpect(model().attribute("sortOrder", "ASC"));
  }

  @Test
  void BK01_005_ソート未指定時デフォルト適用() throws Exception {
    // Given
    when(bookService.count(any(), any(), any(), any())).thenReturn(0);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("sortColumn", "bookId"))
        .andExpect(model().attribute("sortOrder", "DESC"));
  }

  @Test
  void BK01_006_ページ情報算出_端数あり() throws Exception {
    // Given: 該当書籍41件
    when(bookService.count(isNull(), isNull(), isNull(), isNull())).thenReturn(41);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(20));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then: 1ページ20件 -> 41件で3ページ
    mockMvc.perform(get("/book/list").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("totalPages", 3))
        .andExpect(model().attribute("totalCount", 41));
  }

  @Test
  void BK01_007_page指定が下限未満は1に補正() throws Exception {
    // Given
    when(bookService.count(any(), any(), any(), any())).thenReturn(20);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(20));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("currentPage", 1));
  }

  @Test
  void BK01_008_page指定が上限超過は最終ページに補正() throws Exception {
    // Given: 20件 -> 1ページ
    when(bookService.count(any(), any(), any(), any())).thenReturn(20);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(20));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list").param("page", "99"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("currentPage", 1));
  }

  @Test
  void BK01_009_該当書籍0件で未登録メッセージ表示() throws Exception {
    // Given
    when(bookService.count(isNull(), isNull(), isNull(), isNull())).thenReturn(0);
    when(bookService.findAll(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("noDataMessage", "書籍が登録されていません"));
  }

  @Test
  void BK01_010_検索条件ありで検索結果メッセージ表示() throws Exception {
    // Given
    when(bookService.count(isNull(), eq("夏目"), isNull(), isNull())).thenReturn(5);
    when(bookService.findAll(isNull(), eq("夏目"), isNull(), isNull(), any(), any(), anyInt(), anyInt()))
        .thenReturn(createBooks(5));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());
    when(bookService.findAllPublishers()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/list").param("searchAuthor", "夏目"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("searchResultMessage", "5件の書籍が見つかりました"));
  }

  @Test
  void BK01_011_クリア押下でセッション削除しリダイレクト() throws Exception {
    // Given: セッションに検索条件あり
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_SEARCH_TITLE, "Java");
    session.setAttribute(BookConstants.SESSION_SEARCH_AUTHOR, "夏目");
    session.setAttribute(BookConstants.SESSION_SEARCH_CATEGORY_ID, 1);
    session.setAttribute(BookConstants.SESSION_SEARCH_PUBLISHER, "オーム社");

    // When & Then
    mockMvc.perform(get("/book/list/clear").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/list"));

    org.assertj.core.api.Assertions.assertThat(session.getAttribute(BookConstants.SESSION_SEARCH_TITLE))
        .isNull();
    org.assertj.core.api.Assertions.assertThat(session.getAttribute(BookConstants.SESSION_SEARCH_AUTHOR))
        .isNull();
    org.assertj.core.api.Assertions.assertThat(session.getAttribute(BookConstants.SESSION_SEARCH_CATEGORY_ID))
        .isNull();
    org.assertj.core.api.Assertions.assertThat(session.getAttribute(BookConstants.SESSION_SEARCH_PUBLISHER))
        .isNull();
  }
}
