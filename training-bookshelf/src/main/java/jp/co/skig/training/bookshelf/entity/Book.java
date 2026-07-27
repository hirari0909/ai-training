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
