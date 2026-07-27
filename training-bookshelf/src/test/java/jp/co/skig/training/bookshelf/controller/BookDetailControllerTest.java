package jp.co.skig.training.bookshelf.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.entity.Review;
import jp.co.skig.training.bookshelf.service.BookService;
import jp.co.skig.training.bookshelf.service.ReviewService;
import jp.co.skig.training.bookshelf.util.MessageUtilTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookDetailController.class)
class BookDetailControllerTest {

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

  private Book createBook() {
    Book book = new Book();
    book.setBookId(1);
    book.setTitle("タイトル");
    return book;
  }

  private Review createReview(int rating) {
    Review review = new Review();
    review.setBookId(1);
    review.setRating(rating);
    return review;
  }

  @Test
  void BK02_001_正常表示_レビューあり() throws Exception {
    // Given
    Book book = createBook();
    List<Review> reviews = List.of(createReview(5), createReview(3));
    when(bookService.findById(1)).thenReturn(book);
    when(reviewService.findByBookId(1)).thenReturn(reviews);
    when(reviewService.calculateAverageRating(reviews)).thenReturn(4.0);

    // When & Then
    mockMvc.perform(get("/book/detail/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/BK02_BookDetail"))
        .andExpect(model().attribute("book", book))
        .andExpect(model().attribute("reviews", reviews))
        .andExpect(model().attribute("avgRating", 4.0));
  }

  @Test
  void BK02_002_書籍が存在しない場合エラー画面() throws Exception {
    // Given
    when(bookService.findById(999)).thenReturn(null);

    // When & Then
    mockMvc.perform(get("/book/detail/999"))
        .andExpect(status().isOk())
        .andExpect(view().name("book/error"))
        .andExpect(model().attribute("errorMessage", "指定された書籍が見つかりません"))
        .andExpect(model().attribute("redirectUrl", "/book/list"));
  }
}
