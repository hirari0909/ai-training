package jp.co.skig.training.bookshelf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.mapper.BookMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookMapper bookMapper;

  @InjectMocks
  private BookService bookService;

  private Book createBook(int bookId) {
    Book book = new Book();
    book.setBookId(bookId);
    book.setTitle("タイトル" + bookId);
    book.setAuthor("著者" + bookId);
    return book;
  }

  @Test
  void findAll_001_正常取得_offset計算() {
    // Given: page=2, pageSize=20 -> offset=40
    List<Book> expected = List.of(createBook(1), createBook(2));
    when(bookMapper.findAll(eq("Java"), eq("夏目"), eq(1), eq("オーム社"), eq("title"), eq("ASC"), eq(20), eq(40)))
        .thenReturn(expected);

    // When
    List<Book> actual = bookService.findAll("Java", "夏目", 1, "オーム社", "title", "ASC", 2, 20);

    // Then
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0).getBookId()).isEqualTo(1);
    assertThat(actual.get(1).getBookId()).isEqualTo(2);
    verify(bookMapper, times(1)).findAll("Java", "夏目", 1, "オーム社", "title", "ASC", 20, 40);
  }

  @Test
  void count_002_正常取得() {
    // Given
    when(bookMapper.count("Java", null, null, null)).thenReturn(5);

    // When
    int actual = bookService.count("Java", null, null, null);

    // Then
    assertThat(actual).isEqualTo(5);
  }

  @Test
  void findAllPublishers_010_重複排除済み出版社一覧を取得() {
    // Given
    List<String> expected = List.of("オーム社", "岩波書店");
    when(bookMapper.findAllPublishers()).thenReturn(expected);

    // When
    List<String> actual = bookService.findAllPublishers();

    // Then
    assertThat(actual).containsExactly("オーム社", "岩波書店");
    verify(bookMapper, times(1)).findAllPublishers();
  }

  @Test
  void findById_003_存在する書籍を取得() {
    // Given
    Book expected = createBook(1);
    when(bookMapper.findById(1)).thenReturn(expected);

    // When
    Book actual = bookService.findById(1);

    // Then
    assertThat(actual.getBookId()).isEqualTo(1);
    assertThat(actual.getTitle()).isEqualTo("タイトル1");
  }

  @Test
  void findById_004_存在しない場合はnull() {
    // Given
    when(bookMapper.findById(999)).thenReturn(null);

    // When
    Book actual = bookService.findById(999);

    // Then
    assertThat(actual).isNull();
  }

  @Test
  void isDuplicateIsbn_005_重複あり() {
    // Given
    when(bookMapper.findByIsbn("1234567890")).thenReturn(createBook(1));

    // When
    boolean actual = bookService.isDuplicateIsbn("1234567890");

    // Then
    assertThat(actual).isTrue();
  }

  @Test
  void isDuplicateIsbn_006_重複なし() {
    // Given
    when(bookMapper.findByIsbn("1234567890")).thenReturn(null);

    // When
    boolean actual = bookService.isDuplicateIsbn("1234567890");

    // Then
    assertThat(actual).isFalse();
  }

  @Test
  void isDuplicateIsbnWithId_007_重複だが自分自身は除外() {
    // Given: 既存のbookIdが自分自身(1)と一致
    when(bookMapper.findByIsbn("1234567890")).thenReturn(createBook(1));

    // When
    boolean actual = bookService.isDuplicateIsbn("1234567890", 1);

    // Then
    assertThat(actual).isFalse();
  }

  @Test
  void isDuplicateIsbnWithId_008_重複かつ別の書籍() {
    // Given: 既存のbookIdが自分自身(1)と異なる(2)
    when(bookMapper.findByIsbn("1234567890")).thenReturn(createBook(2));

    // When
    boolean actual = bookService.isDuplicateIsbn("1234567890", 1);

    // Then
    assertThat(actual).isTrue();
  }

  @Test
  void isDuplicateIsbnWithId_009_重複なし() {
    // Given
    when(bookMapper.findByIsbn("1234567890")).thenReturn(null);

    // When
    boolean actual = bookService.isDuplicateIsbn("1234567890", 1);

    // Then
    assertThat(actual).isFalse();
  }

  @Test
  void register_010_正常登録() {
    // Given
    Book book = createBook(1);

    // When
    bookService.register(book);

    // Then
    verify(bookMapper, times(1)).insert(book);
  }

  @Test
  void update_011_正常更新() {
    // Given
    Book book = createBook(1);
    when(bookMapper.update(book)).thenReturn(1);

    // When
    int actual = bookService.update(book);

    // Then
    assertThat(actual).isEqualTo(1);
  }

  @Test
  void update_012_楽観ロック失敗() {
    // Given
    Book book = createBook(1);
    when(bookMapper.update(book)).thenReturn(0);

    // When
    int actual = bookService.update(book);

    // Then
    assertThat(actual).isEqualTo(0);
  }

  @Test
  void delete_013_正常削除() {
    // Given
    when(bookMapper.delete(1)).thenReturn(1);

    // When
    int actual = bookService.delete(1);

    // Then
    assertThat(actual).isEqualTo(1);
  }

  @Test
  void delete_014_対象なし() {
    // Given
    when(bookMapper.delete(999)).thenReturn(0);

    // When
    int actual = bookService.delete(999);

    // Then
    assertThat(actual).isEqualTo(0);
  }
}
