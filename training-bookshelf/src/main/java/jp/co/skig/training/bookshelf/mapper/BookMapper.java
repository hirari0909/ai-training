package jp.co.skig.training.bookshelf.mapper;

import java.util.List;
import jp.co.skig.training.bookshelf.entity.Book;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 書籍Mapper
 */
@Mapper
public interface BookMapper {

  /**
   * 書籍一覧を取得する（カテゴリ名・平均評価・レビュー数付き）
   * 動的SQLが必要なためXMLで定義
   */
  List<Book> findAll(
      @Param("searchTitle") String searchTitle,
      @Param("searchAuthor") String searchAuthor,
      @Param("searchCategoryId") Integer searchCategoryId,
      @Param("searchPublisher") String searchPublisher,
      @Param("sortColumn") String sortColumn,
      @Param("sortOrder") String sortOrder,
      @Param("limit") int limit,
      @Param("offset") int offset);

  /**
   * 検索条件に合致する書籍の件数を取得する
   * 動的SQLが必要なためXMLで定義
   */
  int count(
      @Param("searchTitle") String searchTitle,
      @Param("searchAuthor") String searchAuthor,
      @Param("searchCategoryId") Integer searchCategoryId,
      @Param("searchPublisher") String searchPublisher);

  /**
   * 書籍に登録済みの出版社を重複排除して取得する（プルダウン用）
   * マスターテーブルは持たず、書籍テーブルの出版社列から取得する
   * @return 出版社名のリスト（五十音順）
   */
  @Select("SELECT DISTINCT publisher FROM books ORDER BY publisher")
  List<String> findAllPublishers();

  /**
   * 書籍を1件取得する（カテゴリ名付き）
   * @param bookId 書籍ID
   * @return 書籍情報
   */
  @Select("""
      SELECT b.book_id, b.title, b.author, b.publisher, b.published_date,
             b.isbn, b.category_id, b.price, b.description, b.is_recommended AS recommended,
             b.created_at, b.updated_at, c.category_name
      FROM books b
      INNER JOIN categories c ON b.category_id = c.category_id
      WHERE b.book_id = #{bookId}
      """)
  Book findById(Integer bookId);

  /**
   * ISBNから書籍を1件取得する
   * @param isbn ISBN
   * @return 書籍情報
   */
  @Select("SELECT book_id, title, author, publisher, published_date, isbn, "
      + "category_id, price, description, created_at, updated_at "
      + "FROM books WHERE isbn = #{isbn}")
  Book findByIsbn(String isbn);

  /**
   * 書籍を登録する
   * @param book 書籍情報
   */
  @Insert("""
      INSERT INTO books (title, author, publisher, published_date, isbn,
                         category_id, price, description, is_recommended, created_at, updated_at)
      VALUES (#{title}, #{author}, #{publisher}, #{publishedDate}, #{isbn},
              #{categoryId}, #{price}, #{description}, #{recommended}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      """)
  @Options(useGeneratedKeys = true, keyProperty = "bookId")
  void insert(Book book);

  /**
   * 書籍を更新する
   * @param book 書籍情報
   * @return 更新件数（楽観的ロックで0の場合は他ユーザーが更新済み）
   */
  @Update("""
      UPDATE books SET title = #{title}, author = #{author}, publisher = #{publisher},
             published_date = #{publishedDate}, isbn = #{isbn}, category_id = #{categoryId},
             price = #{price}, description = #{description}, is_recommended = #{recommended},
             updated_at = CURRENT_TIMESTAMP
      WHERE book_id = #{bookId} AND updated_at = #{updatedAt}
      """)
  int update(Book book);

  /**
   * 書籍を削除する
   * @param bookId 書籍ID
   * @return 削除件数
   */
  @Delete("DELETE FROM books WHERE book_id = #{bookId}")
  int delete(Integer bookId);
}
