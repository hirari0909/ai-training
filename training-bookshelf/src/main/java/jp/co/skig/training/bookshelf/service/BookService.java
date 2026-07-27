package jp.co.skig.training.bookshelf.service;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Book;
import jp.co.skig.training.bookshelf.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 書籍サービス
 */
@Service
@RequiredArgsConstructor
public class BookService {

  private final BookMapper bookMapper;

  /**
   * 書籍一覧を取得する
   * @param searchTitle タイトル検索条件
   * @param searchAuthor 著者検索条件
   * @param searchCategoryId カテゴリID検索条件
   * @param sortColumn ソート列
   * @param sortOrder ソート順
   * @param page ページ番号（0始まり）
   * @param pageSize 1ページあたりの件数
   * @return 書籍一覧
   */
  public List<Book> findAll(String searchTitle, String searchAuthor,
      Integer searchCategoryId, String sortColumn, String sortOrder,
      int page, int pageSize) {
    int offset = page * pageSize;
    return bookMapper.findAll(searchTitle, searchAuthor, searchCategoryId,
        sortColumn, sortOrder, pageSize, offset);
  }

  /**
   * 検索条件に合致する書籍の件数を取得する
   * @param searchTitle タイトル検索条件
   * @param searchAuthor 著者検索条件
   * @param searchCategoryId カテゴリID検索条件
   * @return 件数
   */
  public int count(String searchTitle, String searchAuthor,
      Integer searchCategoryId) {
    return bookMapper.count(searchTitle, searchAuthor, searchCategoryId);
  }

  /**
   * 書籍を1件取得する
   * @param bookId 書籍ID
   * @return 書籍情報
   */
  public Book findById(Integer bookId) {
    return bookMapper.findById(bookId);
  }

  /**
   * ISBNの重複チェック
   * @param isbn ISBN
   * @return 重複している場合true
   */
  public boolean isDuplicateIsbn(String isbn) {
    return bookMapper.findByIsbn(isbn) != null;
  }

  /**
   * ISBNの重複チェック（自身を除く）
   * @param isbn ISBN
   * @param bookId 除外する書籍ID
   * @return 重複している場合true
   */
  public boolean isDuplicateIsbn(String isbn, Integer bookId) {
    Book existing = bookMapper.findByIsbn(isbn);
    return existing != null && !existing.getBookId().equals(bookId);
  }

  /**
   * 書籍を登録する
   * @param book 書籍情報
   */
  @Transactional
  public void register(Book book) {
    bookMapper.insert(book);
  }

  /**
   * 書籍を更新する
   * @param book 書籍情報
   * @return 更新件数（楽観的ロックで0の場合は他ユーザーが更新済み）
   */
  @Transactional
  public int update(Book book) {
    return bookMapper.update(book);
  }

  /**
   * 書籍を削除する
   * @param bookId 書籍ID
   * @return 削除件数
   */
  @Transactional
  public int delete(Integer bookId) {
    return bookMapper.delete(bookId);
  }
}
