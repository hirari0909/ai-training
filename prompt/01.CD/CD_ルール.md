# CDルール
このドキュメントでは、CD（コーディング実行）を行う際のルールや手順について説明します。

# クラス名・メソッド名
モジュール一覧.mdに記載されているクラス名・メソッド名を遵守してください。

# Sample処理
サンプルコードを下記にコンポーネント種類別に用意しています。  
実装パターンを参考にするとと共に、クラスヘッダーやメソッドヘッダーのコメントの書きっぷりも参考にしてください  

## Controllerのサンプルコード :

```java
/**
 * 書籍削除コントローラー（BK09-BK10）
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookDeleteController {

  private final BookService bookService;

  // ========================================
  // BK09: 書籍削除確認画面
  // ========================================

  /**
   * 書籍削除確認画面を表示
   */
  @GetMapping("/book/delete/confirm/{bookId}")
  public String confirmDelete(@PathVariable Integer bookId, Model model) {
    Book book = bookService.findById(bookId);
    if (book == null) {
      model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
      model.addAttribute("redirectUrl", "/book/list");
      return "book/error";
    }

    model.addAttribute("book", book);
    return "book/BK09_BookDeleteConfirm";
  }

  /**
   * 書籍削除処理
   */
  @PostMapping("/book/delete/{bookId}")
  public String delete(@PathVariable Integer bookId, Model model) {
    try {
      int deleteCount = bookService.delete(bookId);
      if (deleteCount == 0) {
        model.addAttribute("errorMessage", MessageUtil.getMessage("error.notfound.book"));
        model.addAttribute("redirectUrl", "/book/list");
        return "book/error";
      }
      return "redirect:/book/delete/complete";
    } catch (Exception e) {
      log.error(MessageUtil.getMessage("log.error.db", "書籍削除"), e);
      Book book = bookService.findById(bookId);
      model.addAttribute("book", book);
      model.addAttribute("errorMessage", MessageUtil.getMessage("db.error.delete"));
      return "book/BK09_BookDeleteConfirm";
    }
  }

  // ========================================
  // BK10: 書籍削除完了画面
  // ========================================

  /**
   * 書籍削除完了画面を表示
   */
  @GetMapping("/book/delete/complete")
  public String deleteComplete() {
    return "book/BK10_BookDeleteComplete";
  }
}
```



## Serviceのサンプルコード :
更新処理を含む場合はtransactionalを付与してください。ビジネスロジックはServiceに記述し、
Controllerは呼び出すだけの形にしてください。
```java
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

```



## Mapperのサンプルコード :
シンプルなSQLは `@Select` / `@Insert` / `@Update` / `@Delete` のアノテーションで記述し、動的SQL（条件分岐・ソート切替など）が必要な場合はXMLに切り出してください。

### Mapperインターフェース（アノテーション方式）
```java
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
      @Param("searchCategoryId") Integer searchCategoryId);

  /**
   * 書籍を1件取得する（カテゴリ名付き）
   * @param bookId 書籍ID
   * @return 書籍情報
   */
  @Select("""
      SELECT b.book_id, b.title, b.author, b.publisher, b.published_date,
             b.isbn, b.category_id, b.price, b.description,
             b.created_at, b.updated_at, c.category_name
      FROM books b
      INNER JOIN categories c ON b.category_id = c.category_id
      WHERE b.book_id = #{bookId}
      """)
  Book findById(Integer bookId);

  /**
   * 書籍を登録する
   * @param book 書籍情報
   */
  @Insert("""
      INSERT INTO books (title, author, publisher, published_date, isbn,
                         category_id, price, description, created_at, updated_at)
      VALUES (#{title}, #{author}, #{publisher}, #{publishedDate}, #{isbn},
              #{categoryId}, #{price}, #{description}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
             price = #{price}, description = #{description}, updated_at = CURRENT_TIMESTAMP
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
```

### Mapper XML（動的SQL方式）
配置：`src/main/resources/jp/co/skig/training/bookshelf/mapper/BookMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="jp.co.skig.training.bookshelf.mapper.BookMapper">

  <select id="findAll" resultType="jp.co.skig.training.bookshelf.entity.Book">
    SELECT
      b.book_id, b.title, b.author, b.publisher, b.published_date,
      b.isbn, b.category_id, b.price, b.description,
      b.created_at, b.updated_at,
      c.category_name,
      COALESCE(AVG(r.rating), 0) AS avg_rating,
      COUNT(r.review_id) AS review_count
    FROM books b
    INNER JOIN categories c ON b.category_id = c.category_id
    LEFT JOIN reviews r ON b.book_id = r.book_id
    <where>
      <if test="searchTitle != null and searchTitle != ''">
        AND b.title LIKE CONCAT('%', #{searchTitle}, '%')
      </if>
      <if test="searchAuthor != null and searchAuthor != ''">
        AND b.author LIKE CONCAT('%', #{searchAuthor}, '%')
      </if>
      <if test="searchCategoryId != null">
        AND b.category_id = #{searchCategoryId}
      </if>
    </where>
    GROUP BY b.book_id, b.title, b.author, b.publisher, b.published_date,
             b.isbn, b.category_id, b.price, b.description,
             b.created_at, b.updated_at, c.category_name
    <choose>
      <when test="sortColumn == 'title'">ORDER BY b.title ${sortOrder}</when>
      <when test="sortColumn == 'author'">ORDER BY b.author ${sortOrder}</when>
      <when test="sortColumn == 'categoryName'">ORDER BY c.category_name ${sortOrder}</when>
      <otherwise>ORDER BY b.book_id DESC</otherwise>
    </choose>
    LIMIT #{limit} OFFSET #{offset}
  </select>

  <select id="count" resultType="int">
    SELECT COUNT(*)
    FROM books b
    <where>
      <if test="searchTitle != null and searchTitle != ''">
        AND b.title LIKE CONCAT('%', #{searchTitle}, '%')
      </if>
      <if test="searchAuthor != null and searchAuthor != ''">
        AND b.author LIKE CONCAT('%', #{searchAuthor}, '%')
      </if>
      <if test="searchCategoryId != null">
        AND b.category_id = #{searchCategoryId}
      </if>
    </where>
  </select>

</mapper>
```


## Entityのサンプクラスコード :
DBの1レコード／JOIN結果を保持するクラス。Lombokの`@Data`でgetter/setterを自動生成。JOINや集計で取得する派生項目もフィールドとして持たせる。

```java
package jp.co.skig.training.bookshelf.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 書籍エンティティ
 */
@Data
public class Book {
  private Integer bookId;
  private String title;
  private String author;
  private String publisher;
  private LocalDate publishedDate;
  private String isbn;
  private Integer categoryId;
  private Integer price;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /** JOINで取得するカテゴリ名 */
  private String categoryName;

  /** 集計で取得する平均評価 */
  private Double avgRating;

  /** 集計で取得するレビュー数 */
  private Integer reviewCount;
}
```


## Thymeleafのサンプルコード :
- 設計MockHTML（`Docsシステム/外部設計/05個別画面設計/html/`）の構造をそのまま流用し、`th:` 属性で値・リンク・条件分岐・繰り返しを組み込む
- URLは `@{...}` 構文、変数バインドは `${...}`
- フォーム送信先は `th:action="@{/path/{id}(id=${entity.id})}"` のように動的生成

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>書籍削除確認 - 書籍管理システム</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
  <header class="header">
    <h1>書籍管理システム</h1>
    <nav><a th:href="@{/book/list}">書籍一覧</a></nav>
  </header>

  <div class="container">
    <h2 class="page-title">書籍削除確認</h2>

    <div class="alert alert-warning">以下の書籍を削除しますか?</div>
    <div class="alert alert-danger">※この操作は取り消せません。関連するレビューも全て削除されます。</div>

    <!-- エラーメッセージ表示（Controllerからmodelで渡す） -->
    <div class="alert alert-danger" th:if="${errorMessage}" th:text="${errorMessage}"></div>

    <div class="card">
      <dl class="confirm-table">
        <dt>書籍ID</dt>
        <dd th:text="${book.bookId}"></dd>
        <dt>タイトル</dt>
        <dd th:text="${book.title}"></dd>
        <dt>著者</dt>
        <dd th:text="${book.author}"></dd>
      </dl>

      <div class="btn-group">
        <!-- POST送信：URL内にbookIdを動的に埋め込む -->
        <form th:action="@{/book/delete/{id}(id=${book.bookId})}" method="post" style="display:inline;">
          <button type="submit" class="btn btn-danger">削除</button>
        </form>
        <a th:href="@{/book/detail/{id}(id=${book.bookId})}" class="btn btn-secondary">キャンセル</a>
      </div>
    </div>
  </div>

  <footer class="footer">&copy; 2024 Training BookShelf</footer>
</body>
</html>
```









# 定数について
定数は、定数クラスを作成して管理してください。
ルールは以下の通りです。
- クラス名：`{機能名}Constants`（例：`BookConstants`）
- クラス横断：共通で使用する定数は`CommonConstants`クラスにまとめる



## 業務共通処理の利用
下記用途においては、業務共通処理を利用してください。

- メッセージを取得・利用する場合は、MessageUtilクラスを利用すること。
  - 例）MessageUtil.getMessage("E001", "ユーザー")

- ログは監視対象ログに出力するため、ExceptionLoggerクラスを利用すること。
  - 例）ExceptionLogger.log(e)

