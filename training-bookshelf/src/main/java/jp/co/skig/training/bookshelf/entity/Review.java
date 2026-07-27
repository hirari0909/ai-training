package jp.co.skig.training.bookshelf.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * レビューエンティティ
 */
@Data
public class Review {
  private Integer reviewId;
  private Integer bookId;
  private String reviewerName;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
}
