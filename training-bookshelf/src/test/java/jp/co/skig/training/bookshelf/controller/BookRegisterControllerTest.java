package jp.co.skig.training.bookshelf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import java.util.Collections;
import java.util.List;
import jp.co.skig.training.bookshelf.constants.BookConstants;
import jp.co.skig.training.bookshelf.entity.Category;
import jp.co.skig.training.bookshelf.form.BookRegisterForm;
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

@WebMvcTest(BookRegisterController.class)
class BookRegisterControllerTest {

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

  private Category createCategory(int id, String name) {
    Category category = new Category();
    category.setCategoryId(id);
    category.setCategoryName(name);
    return category;
  }

  @Test
  void BK03_001_初期表示_セッションフォームなし() throws Exception {
    // Given
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/create"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK03_BookRegisterInput"))
        .andExpect(model().attribute("form", new BookRegisterForm()));
  }

  @Test
  void BK03_002_セッションフォーム復元() throws Exception {
    // Given
    BookRegisterForm sessionForm = new BookRegisterForm();
    sessionForm.setTitle("保存済みタイトル");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, sessionForm);
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/book/create").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", sessionForm));
  }

  @Test
  void BK03_003_入力値正常で確認画面へ遷移() throws Exception {
    // Given
    when(categoryService.findAll())
        .thenReturn(List.of(createCategory(1, "小説・文学")));

    // When
    var result = mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("description", "概要"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andExpect(model().attribute("categoryName", "小説・文学"))
        .andReturn();

    // Then: セッションにフォームが保存されている
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    BookRegisterForm saved = (BookRegisterForm) session.getAttribute(BookConstants.SESSION_REGISTER_FORM);
    assertThat(saved.getTitle()).isEqualTo("タイトル");
  }

  @Test
  void BK03_004_バリデーションエラーで入力画面再表示() throws Exception {
    // Given: title未入力
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/create/confirm")
            .param("title", "")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK03_BookRegisterInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("title", "タイトルは必須です")));
  }

  @Test
  void BK03_005_出版日不正値は保存時にnullとなる() throws Exception {
    // Given
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then: 不正な日付形式 -> validate()もエラー、変換も失敗
    mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "invalid-date")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK03_BookRegisterInput"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("publishedDate", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK03_006_カテゴリ不正値は保存時にnullとなる() throws Exception {
    // Given: カテゴリが数値でない場合、必須チェックは通過するがInteger変換に失敗しnullとなる
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "abc")
            .param("price", "1500"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("categoryId", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK03_007_価格不正値は保存時にnullとなる() throws Exception {
    // Given: 価格が数値でない場合、validate()でエラーとなり入力画面に戻る
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "abc"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK03_BookRegisterInput"))
        .andExpect(model().attribute("form",
            org.hamcrest.Matchers.hasProperty("price", org.hamcrest.Matchers.nullValue())));
  }

  @Test
  void BK03_008_キャンセルでセッション削除し一覧へ() throws Exception {
    // Given
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, new BookRegisterForm());

    // When & Then
    mockMvc.perform(get("/book/create/cancel").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/list"));

    assertThat(session.getAttribute(BookConstants.SESSION_REGISTER_FORM)).isNull();
  }

  @Test
  void BK03_009_お勧めフラグONで確認画面へ遷移() throws Exception {
    // Given
    when(categoryService.findAll())
        .thenReturn(List.of(createCategory(1, "小説・文学")));

    // When
    var result = mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("description", "概要")
            .param("recommended", "on"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andReturn();

    // Then: セッションにフォームが保存され、お勧めフラグがtrueとなっている
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    BookRegisterForm saved = (BookRegisterForm) session.getAttribute(BookConstants.SESSION_REGISTER_FORM);
    assertThat(saved.getRecommended()).isTrue();
  }

  @Test
  void BK03_010_お勧めフラグ未指定で確認画面へ遷移() throws Exception {
    // Given
    when(categoryService.findAll())
        .thenReturn(List.of(createCategory(1, "小説・文学")));

    // When: recommendedパラメータなし（チェックなし）
    var result = mockMvc.perform(post("/book/create/confirm")
            .param("title", "タイトル")
            .param("author", "著者")
            .param("publisher", "出版社")
            .param("publishedDate", "2024-01-01")
            .param("isbn", "1234567890")
            .param("category", "1")
            .param("price", "1500")
            .param("description", "概要"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andReturn();

    // Then: お勧めフラグがfalseとなっている
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    BookRegisterForm saved = (BookRegisterForm) session.getAttribute(BookConstants.SESSION_REGISTER_FORM);
    assertThat(saved.getRecommended()).isFalse();
  }

  @Test
  void BK04_001_セッション切れ() throws Exception {
    // Given: セッションにフォームなし
    // When & Then
    mockMvc.perform(post("/book/create"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/create"))
        .andExpect(flash().attribute("errorMessage", "セッションが切れました。最初からやり直してください。"));
  }

  @Test
  void BK04_002_ISBN重複エラー() throws Exception {
    // Given
    BookRegisterForm form = new BookRegisterForm();
    form.setIsbn("1234567890");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890")).thenReturn(true);
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/create").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andExpect(model().attribute("errorMessage", "このISBNは既に登録されています"));
  }

  @Test
  void BK04_003_正常登録成功() throws Exception {
    // Given
    BookRegisterForm form = new BookRegisterForm();
    form.setIsbn("1234567890");
    form.setTitle("タイトル");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890")).thenReturn(false);

    // When & Then
    mockMvc.perform(post("/book/create").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/create/complete?bookId=null"));

    verify(bookService, times(1)).register(any());
    assertThat(session.getAttribute(BookConstants.SESSION_REGISTER_FORM)).isNull();
  }

  @Test
  void BK04_004_DB例外発生時() throws Exception {
    // Given
    BookRegisterForm form = new BookRegisterForm();
    form.setIsbn("1234567890");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890")).thenReturn(false);
    doThrow(new RuntimeException("DB error")).when(bookService).register(any());
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(post("/book/create").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK04_BookRegisterConfirm"))
        .andExpect(model().attribute("errorMessage", "データの登録に失敗しました"));
  }

  @Test
  void BK04_005_お勧めフラグがBookエンティティへ反映される() throws Exception {
    // Given
    BookRegisterForm form = new BookRegisterForm();
    form.setIsbn("1234567890");
    form.setTitle("タイトル");
    form.setRecommended(true);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(BookConstants.SESSION_REGISTER_FORM, form);
    when(bookService.isDuplicateIsbn("1234567890")).thenReturn(false);

    // When
    mockMvc.perform(post("/book/create").session(session))
        .andExpect(status().is3xxRedirection());

    // Then: bookService.registerに渡されるBookのrecommendedがtrue
    var captor = org.mockito.ArgumentCaptor.forClass(jp.co.skig.training.bookshelf.entity.Book.class);
    verify(bookService, times(1)).register(captor.capture());
    assertThat(captor.getValue().getRecommended()).isTrue();
  }

  @Test
  void BK05_001_完了画面表示() throws Exception {
    // When & Then
    mockMvc.perform(get("/book/create/complete").param("bookId", "1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK05_BookRegisterComplete"))
        .andExpect(model().attribute("bookId", 1));
  }
}
