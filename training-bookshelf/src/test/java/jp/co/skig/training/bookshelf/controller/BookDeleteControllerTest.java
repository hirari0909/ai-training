package jp.co.skig.training.bookshelf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.util.MessageUtilTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookDeleteController.class)
class BookDeleteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

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
  void BK09_001_正常表示() throws Exception {
    // Given
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(get("/book/delete/confirm/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK09_BookDeleteConfirm"))
        .andExpect(model().attribute("book",
            org.hamcrest.Matchers.hasProperty("bookId", org.hamcrest.Matchers.is(1))));
  }

  @Test
  void BK09_002_書籍が存在しない場合エラー画面() throws Exception {
    // Given
    when(bookService.findById(999)).thenReturn(null);

    // When & Then
    mockMvc.perform(get("/book/delete/confirm/999"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/error"))
        .andExpect(model().attribute("errorMessage", "指定された書籍が見つかりません"));
  }

  @Test
  void BK09_003_正常削除成功() throws Exception {
    // Given
    when(bookService.delete(1)).thenReturn(1);

    // When & Then
    mockMvc.perform(post("/book/delete/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/book/delete/complete"));
  }

  @Test
  void BK09_004_対象が既に存在しない() throws Exception {
    // Given
    when(bookService.delete(999)).thenReturn(0);

    // When & Then
    mockMvc.perform(post("/book/delete/999"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/error"))
        .andExpect(model().attribute("errorMessage", "指定された書籍が見つかりません"));
  }

  @Test
  void BK09_005_DB例外発生時() throws Exception {
    // Given
    doThrow(new RuntimeException("DB error")).when(bookService).delete(1);
    when(bookService.findById(1)).thenReturn(createBook(1));

    // When & Then
    mockMvc.perform(post("/book/delete/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK09_BookDeleteConfirm"))
        .andExpect(model().attribute("errorMessage", "データの削除に失敗しました"));
  }

  @Test
  void BK10_001_完了画面表示() throws Exception {
    // When & Then
    mockMvc.perform(get("/book/delete/complete"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK10_BookDeleteComplete"));
  }
}
