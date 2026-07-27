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

import java.util.Map;
import jp.co.skig.training.bookshelf.constants.ReviewConstants;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.form.ReviewForm;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.ReviewService;
import jp.co.skig.training.bookshelf.util.MessageUtilTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @MockitoBean
  private ReviewService reviewService;

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

  @Test
  void BK11_001_初期表示_セッションフォームなし() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(get("/books/1/reviews/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("form", new ReviewForm()));
  }

  @Test
  void BK11_002_セッションフォーム復元() throws Exception {
    // Given
    ReviewForm sessionForm = new ReviewForm();
    sessionForm.setBookId(1);
    sessionForm.setReviewerName("山田");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, sessionForm);
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(get("/books/1/reviews/new").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", sessionForm));
  }

  @Test
  void BK11_003_書籍が存在しない場合一覧へリダイレクト() throws Exception {
    // Given
    when(bookService.findById(999)).thenReturn(null);

    // When & Then
    mockMvc.perform(get("/books/999/reviews/new"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/list"));
  }

  @Test
  void BK11_013_セッションフォームが別bookIdなら新規Formを使用() throws Exception {
    // Given: セッションのフォームは別書籍(bookId=2)のもの
    ReviewForm sessionForm = new ReviewForm();
    sessionForm.setBookId(2);
    sessionForm.setReviewerName("別の書籍のレビュー");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, sessionForm);
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then: 新規のReviewFormが使用される
    mockMvc.perform(get("/books/1/reviews/new").session(session))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", new ReviewForm()));
  }

  @Test
  void BK11_004_入力値正常で確認画面へ遷移() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When
    var result = mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "5")
            .param("comment", "面白かった"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK12_ReviewPostConfirm"))
        .andReturn();

    // Then
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    ReviewForm saved = (ReviewForm) session.getAttribute(ReviewConstants.SESSION_REVIEW_FORM);
    assertThat(saved.getReviewerName()).isEqualTo("山田");
    assertThat(saved.getRating()).isEqualTo(5);
  }

  @Test
  void BK11_005_書籍が存在しない場合一覧へリダイレクト_confirm() throws Exception {
    // Given
    when(bookService.findById(999)).thenReturn(null);

    // When & Then
    mockMvc.perform(post("/books/999/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "5"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/list"));
  }

  @Test
  void BK11_006_レビュアー名未入力() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "")
            .param("rating", "5"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("reviewerName", "レビュアー名は必須です")));
  }

  @Test
  void BK11_007_レビュアー名文字数超過() throws Exception {
    // Given: 51文字
    when(bookService.findById(1)).thenReturn(createBook(1));
    String longName = "あ".repeat(51);

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", longName)
            .param("rating", "5"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("reviewerName", "レビュアー名は50文字以内で入力してください")));
  }

  @Test
  void BK11_008_評価未入力() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("rating", "評価は必須です")));
  }

  @Test
  void BK11_009_評価が数値でない() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "abc"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("rating", "評価は1〜5の範囲で入力してください")));
  }

  @Test
  void BK11_010_評価が範囲外_上限超過() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "6"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("rating", "評価は1〜5の範囲で入力してください")));
  }

  @Test
  void BK11_014_評価が範囲外_下限未満() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "0"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("rating", "評価は1〜5の範囲で入力してください")));
  }

  @Test
  void BK11_011_コメント文字数超過() throws Exception {
    // Given: 1001文字
    when(bookService.findById(1)).thenReturn(createBook(1));
    String longComment = "あ".repeat(1001);

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "5")
            .param("comment", longComment))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK11_ReviewPostInput"))
        .andExpect(model().attribute("errors",
            org.hamcrest.Matchers.hasEntry("comment", "コメントは1,000文字以内で入力してください")));
  }

  @Test
  void BK11_012_コメントは任意項目のため未入力でも正常() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews/confirm")
            .param("reviewerName", "山田")
            .param("rating", "5"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK12_ReviewPostConfirm"));
  }

  @Test
  void BK12_001_セッション切れ() throws Exception {
    // When & Then: セッションにフォームなし
    mockMvc.perform(post("/books/1/reviews"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/books/1/reviews/new"))
        .andExpect(flash().attribute("errorMessage", "セッションが切れました。最初からやり直してください。"));
  }

  @Test
  void BK12_002_セッションのbookIdと不一致() throws Exception {
    // Given
    ReviewForm form = new ReviewForm();
    form.setBookId(2);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, form);

    // When & Then
    mockMvc.perform(post("/books/1/reviews").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/books/1/reviews/new"))
        .andExpect(flash().attribute("errorMessage", "セッションが切れました。最初からやり直してください。"));
  }

  @Test
  void BK12_003_正常投稿成功() throws Exception {
    // Given
    ReviewForm form = new ReviewForm();
    form.setBookId(1);
    form.setReviewerName("山田");
    form.setRating(5);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, form);

    // When
    var result = mockMvc.perform(post("/books/1/reviews").session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/books/1/reviews/complete"))
        .andReturn();

    // Then
    verify(reviewService, times(1)).register(any());
    MockHttpSession resultSession = (MockHttpSession) result.getRequest().getSession();
    assertThat(resultSession.getAttribute(ReviewConstants.SESSION_REVIEW_FORM)).isNull();
    @SuppressWarnings("unchecked")
    Map<String, Object> completeInfo =
        (Map<String, Object>) resultSession.getAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE);
    assertThat(completeInfo.get("reviewerName")).isEqualTo("山田");
    assertThat(completeInfo.get("rating")).isEqualTo(5);
  }

  @Test
  void BK12_004_DB例外発生時() throws Exception {
    // Given
    ReviewForm form = new ReviewForm();
    form.setBookId(1);
    form.setReviewerName("山田");
    form.setRating(5);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_FORM, form);
    doThrow(new RuntimeException("DB error")).when(reviewService).register(any());
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/books/1/reviews").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK12_ReviewPostConfirm"))
        .andExpect(model().attribute("errorMessage", "データの登録に失敗しました"));
  }

  @Test
  void BK13_001_完了画面表示_完了情報あり() throws Exception {
    // Given
    Map<String, Object> completeInfo = Map.of("reviewerName", "山田", "rating", 5);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE, completeInfo);
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(get("/books/1/reviews/complete").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK13_ReviewPostComplete"))
        .andExpect(model().attribute("reviewerName", "山田"))
        .andExpect(model().attribute("rating", 5));

    assertThat(session.getAttribute(ReviewConstants.SESSION_REVIEW_COMPLETE)).isNull();
  }

  @Test
  void BK13_002_完了情報なしでも表示可能() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(get("/books/1/reviews/complete"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK13_ReviewPostComplete"))
        .andExpect(model().attributeDoesNotExist("reviewerName"));
  }
}
