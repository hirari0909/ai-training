package jp.co.skig.training.bookshelf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.form.BookEditForm;
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

@WebMvcTest(BookEditController.class)
class BookEditControllerTest {

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

  private Book createBook(int bookId) {
    Book book = new Book();
    book.setBookId(bookId);
    book.setTitle("タイトル" + bookId);
    return book;
  }

  private Category createCategory(int id, String name) {
    Category category = new Category();
    category.setCategoryId(id);
    category.setCategoryName(name);
    return category;
  }

  @Test
  void BK06_001_書籍DBから取得して初期表示() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/edit/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK06_BookEditInput"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("bookId", org.hamcrest.Matchers.is(1))));
  }

  @Test
  void BK06_002_セッションフォームが同一bookIdなら復元() throws Exception {
    // Given
    BookEditForm sessionForm = new BookEditForm();
    sessionForm.setBookId(1);
    sessionForm.setTitle("編集中タイトル");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, sessionForm);
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/edit/1").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", sessionForm));
  }

  @Test
  void BK06_003_セッションフォームが別bookIdなら再取得() throws Exception {
    // Given
    BookEditForm sessionForm = new BookEditForm();
    sessionForm.setBookId(2);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, sessionForm);
    when(bookService.findById(1)).thenReturn(createBook(1));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/edit/1").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("bookId", org.hamcrest.Matchers.is(1))));
  }

  @Test
  void BK06_004_書籍が存在しない場合エラー画面() throws Exception {
    // Given
    when(bookService.findById(999)).thenReturn(null);

    // When & Then
    mockMvc.perform(get("/book/edit/999"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/error"))
        .andExpect(model().attribute("errorMessage", "指定された書籍が見つかりません"));
  }

  @Test
  void BK06_005_入力値正常で確認画面へ遷移() throws Exception {
    // Given
    when(categoryService.findAll()).thenReturn(List.of(createCategory(1, "小説・文学")));

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("updatedAt", "2024-01-01T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK07_BookEditConfirm"))
        .andExpect(model().attribute("categoryName", "小説・文学"));
  }

  @Test
  void BK06_006_バリデーションエラーで入力画面再表示() throws Exception {
    // Given: isbn不正
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "123")
            .param("category", "1")
            .param("price", "1500")
            .param("updatedAt", "2024-01-01T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK06_BookEditInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("isbn", "ISBNは10桁または13桁の数字で入力してください")));
  }

  @Test
  void BK06_007_updatedAt不正値はnullとなる() throws Exception {
    // Given
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("updatedAt", "invalid"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK07_BookEditConfirm"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("updatedAt", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK06_009_出版日不正値は変換時にnullとなる() throws Exception {
    // Given: 出版日フォーマット不正 -> validate()もエラーとなる
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "invalid-date")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("updatedAt", "2024-01-01T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK06_BookEditInput"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("publishedDate", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK06_010_カテゴリ不正値は変換時にnullとなる() throws Exception {
    // Given: カテゴリが数値でない場合、必須チェックは通過するがInteger変換に失敗しnullとなる
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "abc")
            .param("price", "1500")
            .param("updatedAt", "2024-01-01T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK07_BookEditConfirm"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("categoryId", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK06_011_価格不正値は変換時にnullとなる() throws Exception {
    // Given: 価格が数値でない場合、validate()でエラーとなり入力画面に戻る
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "abc")
            .param("updatedAt", "2024-01-01T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK06_BookEditInput"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("price", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK06_008_キャンセルでセッション削除し詳細へ() throws Exception {
    // Given
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, new BookEditForm());

    // When & Then
    mockMvc.perform(get("/book/edit/1/cancel").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/detail/1"));

    assertThat(session.getAttribute(BookConstants.SESSION_EDIT_FORM)).isNull();
  }

  @Test
  void BK07_001_セッション切れ() throws Exception {
    // When & Then: セッションにフォームなし
    mockMvc.perform(post("/book/edit/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/edit/1"))
        .andExpect(flash().attribute("errorMessage", "セッションが切れました。最初からやり直してください。"));
  }

  @Test
  void BK07_002_セッションのbookIdと不一致() throws Exception {
    // Given
    BookEditForm form = new BookEditForm();
    form.setBookId(2);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);

    // When & Then
    mockMvc.perform(post("/book/edit/1").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/edit/1"))
        .andExpect(flash().attribute("errorMessage", "セッションが切れました。最初からやり直してください。"));
  }

  @Test
  void BK07_003_ISBN重複エラー() throws Exception {
    // Given
    BookEditForm form = new BookEditForm();
    form.setBookId(1);
    form.setIsbn("1234567890");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890", 1)).thenReturn(true);
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK07_BookEditConfirm"))
        .andExpect(model().attribute("errorMessage", "このISBNは既に登録されています"));
  }

  @Test
  void BK07_004_楽観ロック失敗() throws Exception {
    // Given
    BookEditForm form = new BookEditForm();
    form.setBookId(1);
    form.setIsbn("1234567890");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890", 1)).thenReturn(false);
    when(bookService.update(any())).thenReturn(0);

    // When & Then
    mockMvc.perform(post("/book/edit/1").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/detail/1"))
        .andExpect(flash().attribute("errorMessage", "他のユーザーによって更新されています。最新のデータを取得してください。"));

    assertThat(session.getAttribute(BookConstants.SESSION_EDIT_FORM)).isNull();
  }

  @Test
  void BK07_005_正常更新成功() throws Exception {
    // Given
    BookEditForm form = new BookEditForm();
    form.setBookId(1);
    form.setIsbn("1234567890");
    form.setUpdatedAt(LocalDateTime.now());
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890", 1)).thenReturn(false);
    when(bookService.update(any())).thenReturn(1);

    // When & Then
    mockMvc.perform(post("/book/edit/1").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/edit/complete?bookId=1"));

    verify(bookService, times(1)).update(any());
    assertThat(session.getAttribute(BookConstants.SESSION_EDIT_FORM)).isNull();
  }

  @Test
  void BK07_006_DB例外発生時() throws Exception {
    // Given
    BookEditForm form = new BookEditForm();
    form.setBookId(1);
    form.setIsbn("1234567890");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_EDIT_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890", 1)).thenReturn(false);
    doThrow(new RuntimeException("DB error")).when(bookService).update(any());
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/edit/1").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK07_BookEditConfirm"))
        .andExpect(model().attribute("errorMessage", "データの更新に失敗しました"));
  }

  @Test
  void BK08_001_完了画面表示() throws Exception {
    // When & Then
    mockMvc.perform(get("/book/edit/complete").param("bookId", "1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK08_BookEditComplete"))
        .andExpect(model().attribute("bookId", 1));
  }
}
