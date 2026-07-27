package jp.co.skig.training.bookshelf.form;

import java.io.Serializable;
import lombok.Data;

/**
 * レビュー投稿フォーム
 */
@Data
public class ReviewForm implements Serializable {
  private Integer bookId;
  private String reviewerName;
  private Integer rating;
  private String comment;
}
