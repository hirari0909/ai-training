package jp.co.skig.training.bookshelf.form;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

/**
 * 書籍登録フォーム
 */
@Data
public class BookRegisterForm implements Serializable {
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
}
