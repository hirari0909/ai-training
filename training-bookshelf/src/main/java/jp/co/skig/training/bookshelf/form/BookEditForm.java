package jp.co.skig.training.bookshelf.form;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 書籍編集フォーム
 */
@Data
public class BookEditForm implements Serializable {
  private Integer bookId;
  private String title;
  private String author;
  private String publisher;
  private LocalDate publishedDate;
  private String isbn;
  private Integer categoryId;
  private Integer price;
  private String description;

  /** お勧めフラグ */
  private Boolean recommended;

  /** 楽観的ロック用の更新日時 */
  private LocalDateTime updatedAt;
}
